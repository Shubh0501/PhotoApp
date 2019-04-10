# PhotoApp
version 1.0

PhotoApp lets you either upload pictures from your gallery or take photos from your camera and saves them securely in your device. 

# How to use the App

1. Use the apk located in ./app/release/PhotoApp.apk and install the app in your device.
2. Please accept the permission to access your External Memory since we won't be able to access your images in external memory
   if you don't accept that permission.
3. Choose Either to take a photo from your camera or to upload a photo present in your device.
4. Provide a name by which you want to save the image. If left blank, the image will be saved with a timestamped name.
5. You will be redirected to the page which contains all your images in compressed and encoded form. 
6. Click on any name to view that image and drag it down to see the list again.
7. Long Press on an Image to delete that image.
8. To click/Upload a new picture, click on the button at the top of the screen.

# Things to know

1. If taken a picture using the camera, that picture won't be saved on the memory for user use and only be saved in encoded form.
2. If uploaded a picture already present on the device, that picture won't be deleted from the memory but its encoded form will be saved.
3. The compression algorithm is not very efficient at this moment. It compresses the image a lot. Normal images are not affected much
   but images with text are affected and become unreadable.   
4. The design of the app is not good at this moment.
5. For encryption process AES/Rijndael algorithm is used with CBC(Cipher Block Chaining) mode and PKCS5 padding.
6. For the compression of image, SiliCompressor is used. It can be found at:https://github.com/Tourenathan-G5organisation/SiliCompressor
