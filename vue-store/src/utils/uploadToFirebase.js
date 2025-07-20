import { storage } from '@/plungins/firebase-storage.js'
import { ref as storageRef, uploadBytes, getDownloadURL } from "firebase/storage"

export async function uploadFilesToFirebase(files) {
    const urlList = []
    for (let i = 0; i < files.length; i++) {
        const file = files[i]
        const ext = file.name.split('.').pop()
        const fbRef = storageRef(storage, `stores/${Date.now()}_${Math.random().toString(36).slice(2)}.${ext}`)
        await uploadBytes(fbRef, file)
        const url = await getDownloadURL(fbRef)
        urlList.push(url)
    }
    return urlList
}
