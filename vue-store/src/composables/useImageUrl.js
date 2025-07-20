import defaultPhoto from '@/assets/default-photo.jpg';

export function useImageUrl() {
    const getImageUrl = (imgResource) => {
        // 如果沒有圖片資源，回傳預設圖片
        if (!imgResource) {
            return defaultPhoto;
        }

        // 如果是完整 URL（Firebase 或其他外部 URL），直接使用
        if (imgResource.startsWith('http://') || imgResource.startsWith('https://')) {
            return imgResource;
        }

        // 如果是相對路徑（如 "images/filename.jpg"），轉換為本地路徑
        if (imgResource.startsWith('images/')) {
            // 移除 "images/" 前綴，改為 "/image/"
            const filename = imgResource.replace('images/', '');
            return `/image/${filename}`;
        }

        // 如果已經是 "/image/" 格式，直接使用
        if (imgResource.startsWith('/image/')) {
            return imgResource;
        }

        // 其他情況，嘗試直接使用
        return imgResource;
    };

    return {
        getImageUrl,
        defaultPhoto
    };
}