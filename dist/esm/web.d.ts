import { WebPlugin } from '@capacitor/core';
import type { GalleryCapacitorPlugin, PickFilesOptions, PickFilesResult } from './definitions';
export declare class GalleryCapacitorWeb extends WebPlugin implements GalleryCapacitorPlugin {
    pickFiles(options: PickFilesOptions): Promise<PickFilesResult>;
}
