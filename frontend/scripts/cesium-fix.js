// 修复webpack5下cesium打包会报错问题
const fs = require('fs');
// try to load the file
try {
    const fileName = require.resolve('cesium/package.json');
    const jsonString = fs.readFileSync(fileName);
    const file = JSON.parse(jsonString);
    // add new field for proper exporting widgets.css
    file.exports["./Build/Cesium/Widgets/widgets.css"] = "./Build/Cesium/Widgets/widgets.css";
    // write the file
    fs.writeFile(fileName, JSON.stringify(file), function writeJSON(err) {
        if (err) return console.log(err);
        console.log('writing to ' + fileName);
    });
} catch (err) {
    return;
};
