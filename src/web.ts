import { WebPlugin } from '@capacitor/core';

import type { GalleryCapacitorPlugin, PermissionStatus, PickFilesOptions, PickFilesResult } from './definitions';

export class GalleryCapacitorWeb extends WebPlugin implements GalleryCapacitorPlugin {
  async checkPermissions(): Promise<PermissionStatus> {
    const permissionState: PermissionStatus = { gallery: 'granted' };
    return permissionState;
  }
  async requestPermissions(): Promise<PermissionStatus> {
    const permissionState: PermissionStatus = { gallery: 'granted' };
    return permissionState;
  }
  async pickFiles(options: PickFilesOptions): Promise<PickFilesResult> {
    options.maximumFilesCount = 3;
    let files: any = {}; 
    return files;
  }
}
