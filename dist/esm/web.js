import { WebPlugin } from '@capacitor/core';
export class GalleryCapacitorWeb extends WebPlugin {
    async checkPermissions() {
        const permissionState = { gallery: 'granted' };
        return permissionState;
    }
    async requestPermissions() {
        const permissionState = { gallery: 'granted' };
        return permissionState;
    }
    async pickFiles(options) {
        options.maximumFilesCount = 3;
        let files = {};
        return files;
    }
}
//# sourceMappingURL=web.js.map