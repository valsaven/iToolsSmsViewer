import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import resolve from 'vite-plugin-resolve';
import libEsm from 'lib-esm';
import electron from 'vite-plugin-electron-renderer';
import pkg from '../../package.json';

// https://vitejs.dev/config/
export default defineConfig({
  mode: process.env.NODE_ENV,
  root: __dirname,
  plugins: [
    vue(),
    electron(),
    resolve(
      /**
       * Here you can specify other modules
       * 🚧 You have to make sure that your module is in `dependencies` and not in the` devDependencies`,
       *    which will ensure that the electron-builder can package it correctly
       */
      {
        // If you use the following modules, the following configuration will work
        // What they have in common is that they will return - ESM format code snippets

        // ESM format string
        'electron-store': 'export default require("electron-store");',

        sqlite3: () => {
          const result = libEsm({
            window: 'sqlite3',
          })
          return `${result.window}\n${result.exports}`
        },
        serialport: () => {
          const result = libEsm({
            window: 'serialport',
            exports: [
              'SerialPort',
              'SerialPortMock',
            ],
          })
          return `${result.window}\n${result.exports}`
        },
      }
    ),
  ],
  base: './',
  build: {
    outDir: '../../dist/renderer',
    emptyOutDir: true,
    sourcemap: true,
  },
  server: {
    host: pkg.env.VITE_DEV_SERVER_HOST,
    port: pkg.env.VITE_DEV_SERVER_PORT,
  },
})
