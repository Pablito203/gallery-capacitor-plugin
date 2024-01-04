import { registerPlugin } from '@capacitor/core';
const GalleryCapacitor = registerPlugin('GalleryCapacitor', {
    web: () => import('./web').then(m => new m.GalleryCapacitorWeb()),
});
export * from './definitions';
export { GalleryCapacitor };
//# sourceMappingURL=index.js.map