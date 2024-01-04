'use strict';

Object.defineProperty(exports, '__esModule', { value: true });

var core = require('@capacitor/core');

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
//# sourceMappingURL=plugin.cjs.js.map
