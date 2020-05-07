from scipy import ndimage
import matplotlib.image as mpimg 
import matplotlib.pyplot as plt
import numpy as np
import skimage.morphology as morph
from skimage.filters import threshold_otsu, rank
import skimage.filters  as filters
def rgb2gray(rgb):
    return np.dot(rgb[...,:3], [0.2989, 0.5870, 0.1140])
# images =["gel01.jpg", "gel02.png", "gel03.jpg", "gel04.png", "gel05.png", "gel06.jpg", "gel07.jpg", "gel08.png","gel09.png", \
#          "gel10.png", "gel11.png", "gel12.png", "gel13.jpg", "gel14.png", "gel15.png", "gel16.png", "gel17.png", "gel18.jpg", "gel19.png", "gel20.png"]
images =["gel19.png","gel19_improved.jpg"]
for image in images:
    colorIm =mpimg.imread(f'./images/{image}')
    print(len(colorIm.shape))
    im = colorIm
    if len(colorIm.shape)!=2:
        im = rgb2gray(colorIm)
    print(im.shape)
    im1 = morph.erosion(im,morph.square(round(im.shape[0]/20)))
    im2 = morph.dilation(im,morph.square(round(im.shape[0]/20)))
    print(np.min(im2),np.max(im2))

    fig, axes = plt.subplots(1,2)
    axes[0].imshow(im1, cmap="gray")
    axes[1].imshow(im2, cmap="gray")
    plt.show()