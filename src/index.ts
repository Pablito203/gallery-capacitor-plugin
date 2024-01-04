import { registerPlugin } from '@capacitor/core';

import type { GalleryCapacitorPlugin } from './definitions';

const GalleryCapacitor = registerPlugin<GalleryCapacitorPlugin>('GalleryCapacitor', {
  web: () => import('./web').then(m => new m.GalleryCapacitorWeb()),
});

export * from './definitions';
export { GalleryCapacitor };
