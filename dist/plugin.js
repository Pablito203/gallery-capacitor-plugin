var capacitorGalleryCapacitor = (function (exports, core) {
    'use strict';

    const GalleryCapacitor = core.registerPlugin('GalleryCapacitor', {
        web: () => Promise.resolve().then(function () { return web; }).then(m => new m.GalleryCapacitorWeb()),
    });

    class GalleryCapacitorWeb extends core.WebPlugin {
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

    Object.defineProperty(exports, '__esModule', { value: true });

    return exports;

})({}, capacitorExports);
//# sourceMappingURL=plugin.js.map
