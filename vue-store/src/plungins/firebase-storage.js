// firebase-config.js（兩個專案都需要相同檔案）
import { initializeApp } from 'firebase/app';
import { getStorage, ref, uploadBytes, getDownloadURL } from 'firebase/storage';

const firebaseConfig = {
  // 從 Firebase Console 複製
    apiKey: "AIzaSyBzHfJwAj4pL6Hd0h9XrZMUgn8NdIv2mqI",
    authDomain: "goldenbowl-8e932.firebaseapp.com",
    projectId: "goldenbowl-8e932",
    storageBucket: "goldenbowl-8e932.firebasestorage.app",
    messagingSenderId: "696773334353",
    appId: "1:696773334353:web:c7748f4a60f70cf4ef2f70"
};

const app = initializeApp(firebaseConfig);
export const storage = getStorage(app);

export const uploadImage = async (file) => {
    try {
        const fileName = `${Date.now()}_${file.name}`;
        const storageRef = ref(storage, `images/${fileName}`);
        
        const snapshot = await uploadBytes(storageRef, file);
        const downloadURL = await getDownloadURL(snapshot.ref);
        
        return downloadURL;
    } catch (error) {
        console.error('圖片上傳失敗:', error);
        throw error;
    }
};