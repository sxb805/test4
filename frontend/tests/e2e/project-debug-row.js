const { chromium } = require('playwright-core');
const crypto = require('crypto');

(async () => {
  const baseUrl='http://localhost:8000';
  const md5 = crypto.createHash('md5').update('Vortex2026Info').digest('hex');
  const res = await fetch(`${baseUrl}/cas/login`, {method:'POST', headers:{'Content-Type':'application/json'}, body:JSON.stringify({username:'zhsw_test',password:md5})});
  const json = await res.json();
  const token = JSON.parse(res.headers.get('access-token')).access_token;

  const browser = await chromium.launch({ headless: true, channel: 'chrome' });
  const page = await browser.newPage();
  const url = `${baseUrl}/#/project?token=${encodeURIComponent(token)}&tenantId=${encodeURIComponent(json.data.tenantId)}&userId=${encodeURIComponent(json.data.userId)}`;
  await page.goto(url, {waitUntil:'domcontentloaded'});
  await page.waitForTimeout(2500);
  const row = page.locator('.ant-table-tbody tr').first();
  const html = await row.innerHTML();
  console.log(html);
  await browser.close();
})();
