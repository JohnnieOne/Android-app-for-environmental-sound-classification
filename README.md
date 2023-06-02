# Android-app-for-environmental-sound-classification

The recognition of ambient sounds will be considered a problem for which an optimized sound recognition system will be built using artificial intelligence methods. 
The development platform will be Google Colab or Kaggle (with specific Python libraries, including spectral analysis libraries, Keras/TensorFlow, and others deemed necessary), and the optimized model will be integrated into an Android application through the TFLite library. 
At least two public databases will be used (such as ESC-50, UrbanSound8K), and based on recent approaches from [1][2], an RD-CNN system will be implemented and comparatively analyzed. 
This system consists of the Reaction Diffusion Transform (RDT) for generating spectral images and a low-complexity CNN model for spectrogram recognition. 
The models and hyperparameters will be optimized to select the optimal model in terms of both performance (accuracy on the validation set) and implementation complexity for mobile platforms with the Android operating system. 

Resources used in the project development include relevant articles and other resources: [1] R. Dogaru and Ioana Dogaru, "RD-CNN: A Compact and Efficient Convolutional Neural Net for Sound Classification," in ISETC-2020, Timişoara, 978-1-7281-9513-1/20/.00 ©2020 IEEE; [2] I. Dogaru, C. Stan, R. Dogaru, "Compact Isolated Speech Recognition on Raspberry Pi based on Reaction Diffusion Transform," ISEEE 2019.
