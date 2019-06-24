package Geles;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ngsep.genome.GenomicRegionPositionComparator;
import ngsep.math.PhredScoreHelper;
import ngsep.variants.CalledGenomicVariant;
import ngsep.variants.CalledGenomicVariantImpl;
import ngsep.variants.GenomicVariant;
import ngsep.variants.GenomicVariantImpl;
import ngsep.vcf.VCFFileHeader;
import ngsep.vcf.VCFFileWriter;
import ngsep.vcf.VCFRecord;

public class PosteriorMeanMixtureModelSampleClustering implements SampleClusteringAlgorithm {

	private VCFFileHeader header;
	private List<VCFRecord> records;
	private List<String> alleles;
	private short minQuality = 1;
	
	public PosteriorMeanMixtureModelSampleClustering() {
		alleles = new ArrayList<>();
		alleles.add("BKG");
		alleles.add("BAND");
	}
	@Override
	public double [][] clusterSamples(IntensityProcessor processor) {
		records = new ArrayList<>();
		List<Well> wells = processor.getWells();
		header = VCFFileHeader.makeDefaultEmptyHeader();
		for(Well well:wells) {
			header.addDefaultSample(well.getSampleId());
		}
		List<AlleleBandCluster> alleleClusters = processor.getAlleleClusters();
		int n = wells.size();
		double [][] distanceMatrix = new double [n][n];
		int [][] callsPerDatapoint = new int [n][n]; 
		for(int i=0;i<n;i++) {
			Arrays.fill(distanceMatrix[i], 0);
			Arrays.fill(callsPerDatapoint[i], 0);
		}
		for(AlleleBandCluster cluster:alleleClusters) {
			VCFRecord record = buildVCFRecord(cluster, header, processor);
			updateDistances(record.getCalls(), distanceMatrix, callsPerDatapoint);
			records.add(record);
		}
		Collections.sort(records, GenomicRegionPositionComparator.getInstance());
		for(int i=0;i<n;i++) {
			for(int j=0;j<n;j++) {
				if(callsPerDatapoint[i][j]>0) distanceMatrix[i][j]/=callsPerDatapoint[i][j];
			}
		}
		return distanceMatrix;
	}
	/**
	 * 
	 * @param cluster of bands to make record. PRE: cluster.size()>0
	 * @param wells
	 * @param header
	 * @param processor
	 * @return VCFRecord
	 */
	private VCFRecord buildVCFRecord(AlleleBandCluster cluster, VCFFileHeader header, IntensityProcessor processor) {
		List<Well> wells = processor.getWells();
		List<String> sampleIds = header.getSampleIds();
		int n = sampleIds.size();
		List<CalledGenomicVariant> calls = new ArrayList<>(n);
		for(int i=0;i<n;i++) calls.add(null);
		int first = cluster.getMolecularWeight();
		GenomicVariant variant = new GenomicVariantImpl("Image", first, alleles);
		double sumHeight = 0;
		List<Band> bands = cluster.getBands();
		for(Band b:bands) {
			int sampleIdx = b.getWellPosition();
			CalledGenomicVariant call = new CalledGenomicVariantImpl(variant, CalledGenomicVariant.GENOTYPE_HOMOALT);
			double prob = processor.calculateProbabilityAverageSignalMixtureModel(b.getStartRow(), b.getEndRow(), b.getStartColumn(), b.getEndColumn());
			call.setGenotypeQuality(PhredScoreHelper.calculatePhredScore(1-prob));
			calls.set(sampleIdx, call);
			sumHeight += b.calculateBandHeight();
		}
		int halfRowSize = (int) Math.round(sumHeight / (2.0*bands.size()));
		for(int i=0;i<n;i++) {
			if(calls.get(i)==null) {
				Well well = wells.get(i);
				CalledGenomicVariant call = new CalledGenomicVariantImpl(variant, CalledGenomicVariant.GENOTYPE_HOMOREF);
				int startRow = Math.max(0, first - halfRowSize);
				int endRow = Math.min(processor.getIntensities().length, first + halfRowSize);
				double prob = processor.calculateProbabilityAverageSignalMixtureModel(startRow, endRow, well.getStartCol(), well.getStartCol()+well.getWellWidth());
				call.setGenotypeQuality(PhredScoreHelper.calculatePhredScore(prob));
				calls.set(i, call);
			}
		}
		return new VCFRecord(variant, VCFRecord.DEF_FORMAT_ARRAY_QUALITY, calls, header);
	}
	private void updateDistances(List<CalledGenomicVariant> calls, double[][] distanceMatrix, int[][] callsPerDatapoint) {
		int n = calls.size();
		for(int j=0;j<n;j++){
			CalledGenomicVariant call1 = calls.get(j);
			if(call1.isUndecided() || call1.getGenotypeQuality()<minQuality) continue;
    		for(int k=0;k<n;k++){
    			if(j==k) continue;
    			CalledGenomicVariant call2 = calls.get(k);
    			if(call2.isUndecided() || call2.getGenotypeQuality()<minQuality) continue;
    			//distance between pair of genotypes for a single variant
			    distanceMatrix[j][k] += Math.abs(call1.getIndexesCalledAlleles()[0]-call2.getIndexesCalledAlleles()[0]);
				//matrix needed to save value by how divide
				callsPerDatapoint[j][k]++;
	    	}
    	}
	}
	@Override
	public void saveClusteringData(String outputFilePrefix) throws IOException {
		VCFFileWriter writer = new VCFFileWriter();
		try (PrintStream out = new PrintStream(outputFilePrefix+".vcf")) {
			writer.printHeader(header, out);
			writer.printVCFRecords(records, out);
		}
		
	}
	

}
