from imutils import paths
import cv2
import numpy as np
from matplotlib import pyplot as plt
import base64

def base64ToMat(base64Img):
    # Base64 format picture to opencv mat format
    imgOriginal = base64.b64decode(base64Img)
    imgNp = np.frombuffer(imgOriginal, dtype=np.uint8)
    return cv2.imdecode(imgNp, cv2.IMREAD_UNCHANGED)

def matToBase64(img):
    return base64.b64encode(cv2.imencode('.png', img)[1]).decode()

def get_contrast_ratio(img):
    img = cv2.cvtColor(img, cv2.COLOR_RGB2GRAY)
    hist, bins = np.histogram(img.flatten(), 256, [0, 256])
    cdf = hist.cumsum()
    cdf_normalized = cdf * hist.max() / cdf.max()
    fm = np.max(cdf_normalized)
    fm = np.mean(np.log(fm))*10
    return fm

def getContrastRatioImg(base64Img):
    image = base64ToMat(base64Img)
    fm = get_contrast_ratio(image)
    text= 'contrast_ratio'
    # show the image
    cv2.putText(image, "{}: {:.2f}".format(text, fm), (10, 30),
                   cv2.FONT_HERSHEY_SIMPLEX, 0.8, (0, 0, 255), 3)
    return matToBase64(image)

def getContrastRatioValue(base64Img):
    image = base64ToMat(base64Img)
    return get_contrast_ratio(image)