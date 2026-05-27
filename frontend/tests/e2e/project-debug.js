const { chromium } = require('playwright-core');
const crypto = require('crypto');

const baseUrl='http://localhost:8000';
const username='zhsw_test';
const plainPassword='Vortex2026Info';
const md5Password = crypto.createHash('md5').update(plainPassword).digest('hex');

(async () => {
  const browser = await chromium.launch({ headless: true, channel: 'chrome' });
  const page = await browser.newPage();
  const reqs=[];
  page.on('response', async (resp)=>{
    const u=resp.url();
    if (u.includes('/cloud/sample/project/')) {
      let txt='';
      try { txt = await resp.text(); } catch(e) {}
      reqs.push({url:u,status:resp.status(),body:txt.slice(0,500)});
    }
  });

  const res = await fetch(`${baseUrl}/cas/login`, {method:'POST', headers:{'Content-Type':'application/json'}, body:JSON.stringify({username,password:md5Password})});
  const json = await res.json();
  const accessTokenHeader = res.headers.get('access-token');
  const accessToken = accessTokenHeader ? JSON.parse(accessTokenHeader).access_token : json?.data?.userCode;
  const tenantId = json?.data?.tenantId;
  const userId = json?.data?.userId;

  const url = `${baseUrl}/#/project?token=${encodeURIComponent(accessToken)}&tenantId=${encodeURIComponent(tenantId)}&userId=${encodeURIComponent(userId)}`;
  await page.goto(url, {waitUntil:'domcontentloaded', timeout:30000});
  await page.waitForTimeout(3000);

  const topButtons = await page.locator('button:visible').allTextContents();
  const rowOpTexts = await page.locator('.ant-table-tbody tr td').allTextContents();

  console.log('TOP_BUTTONS=' + JSON.stringify(topButtons));
  console.log('ROW_TEXTS=' + JSON.stringify(rowOpTexts.slice(0,30)));
  console.log('PROJECT_REQS=' + JSON.stringify(reqs, null, 2));

  await browser.close();
})();
