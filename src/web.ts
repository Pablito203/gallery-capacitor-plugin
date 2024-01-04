import { WebPlugin } from '@capacitor/core';

import type { GalleryCapacitorPlugin, PickFilesOptions, PickFilesResult } from './definitions';

export class GalleryCapacitorWeb extends WebPlugin implements GalleryCapacitorPlugin {
  async pickFiles(options: PickFilesOptions): Promise<PickFilesResult> {
    options.maximumFilesCount = 3;
    let files: any = {}; 
    return files;
  }
}
