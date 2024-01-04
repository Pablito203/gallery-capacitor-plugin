import { WebPlugin } from '@capacitor/core';
export class GalleryCapacitorWeb extends WebPlugin {
    async pickFiles(options) {
        options.maximumFilesCount = 3;
        let files = {};
        return files;
    }
}
//# sourceMappingURL=web.js.map