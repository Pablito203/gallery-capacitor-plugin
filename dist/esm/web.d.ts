import { WebPlugin } from '@capacitor/core';
import type { GalleryCapacitorPlugin, PermissionStatus, PickFilesOptions, PickFilesResult } from './definitions';
export declare class GalleryCapacitorWeb extends WebPlugin implements GalleryCapacitorPlugin {
    checkPermissions(): Promise<PermissionStatus>;
    requestPermissions(): Promise<PermissionStatus>;
    pickFiles(options: PickFilesOptions): Promise<PickFilesResult>;
}
