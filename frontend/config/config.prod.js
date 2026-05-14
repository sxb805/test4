import { defineConfig } from "umi";
import fs from 'fs';
import config from './config';
export default defineConfig({
    copy: fs.existsSync('menu.json')
        ? [
            {
                from: 'menu.json',
                to: 'dist/resources/json/menu.json',
            },
            ...(config.copy || []),
        ]
        : [...(config.copy || [])],
    codeSplitting:{
        jsStrategy: 'granularChunks'
    },
});
