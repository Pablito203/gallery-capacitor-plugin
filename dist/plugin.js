var capacitorGalleryCapacitor = (function (exports, core) {
    'use strict';

    const GalleryPermissionType = {
        /**
         * The permission name for Android 13 / API 33 and above.
         */
        TIRAMISU_GALLERY: 'tiramisuGallery',
        /**
         * The permission name for Android 12 / API 32 and below.
         */
        GALLERY: 'gallery'
    };

    const GalleryCapacitor = core.registerPlugin('GalleryCapacitor', {
        web: () => Promise.resolve().then(function () { return web; }).then(m => new m.GalleryCapacitorWeb()),
    });

    class GalleryCapacitorWeb extends core.WebPlugin {
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

    var web = /*#__PURE__*/Object.freeze({
        __proto__: null,
        GalleryCapacitorWeb: GalleryCapacitorWeb
    });

    exports.GalleryCapacitor = GalleryCapacitor;
    exports.GalleryPermissionType = GalleryPermissionType;

    Object.defineProperty(exports, '__esModule', { value: true });

    return exports;

})({}, capacitorExports);
//# sourceMappingURL=plugin.js.map
