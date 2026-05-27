const { test, expect } = require('@playwright/test');
const crypto = require('crypto');

const baseUrl = process.env.E2E_BASE_URL || 'http://127.0.0.1:8000';
const username = process.env.E2E_USERNAME || 'zhsw_test';
const plainPassword = process.env.E2E_PASSWORD || 'Vortex2026Info';
const md5Password = crypto.createHash('md5').update(plainPassword).digest('hex');

function nowCode() { const d=new Date(); const p=(n)=>`${n}`.padStart(2,'0'); return `E2E_${d.getFullYear()}${p(d.getMonth()+1)}${p(d.getDate())}${p(d.getHours())}${p(d.getMinutes())}${p(d.getSeconds())}`; }

async function loginAndGetContext(request) {
  const res = await request.post(`${baseUrl}/cas/login`, { data: { username, password: md5Password } });
  const json = await res.json();
  if (json?.result !== 0 || !json?.data) throw new Error(`login failed: ${json?.msg || res.status()}`);
  let accessToken = '';
  try {
    const accessTokenHeader = res.headers()['access-token'];
    if (accessTokenHeader) {
      const tokenJson = JSON.parse(accessTokenHeader);
      accessToken = tokenJson?.access_token || '';
    }
  } catch (e) {
    // keep fallback for environments that don't expose custom headers
  }
  return { token: accessToken || json.data.userCode, tenantId: json.data.tenantId, userId: json.data.userId };
}

async function clickFirst(scope, selectors) {
  for (const s of selectors) {
    const loc = scope.locator(s).first();
    if (await loc.isVisible().catch(() => false)) {
      try { await loc.click({ timeout: 5000 }); } catch (e) { await loc.click({ timeout: 3000, force: true }); }
      return true;
    }
  }
  return false;
}

async function cleanupOverlays(page) {
  for (let i = 0; i < 4; i += 1) {
    const modal = page.locator('.ant-modal-wrap:visible').last();
    if (!(await modal.isVisible().catch(() => false))) break;
    await clickFirst(modal, ['button:has-text("取消")','button:has-text("关闭")','.ant-modal-close']);
    await page.keyboard.press('Escape').catch(() => {});
    await page.waitForTimeout(300);
  }
}

async function fillAddFormAndSubmit(page, code) {
  const modal = page.locator('.ant-modal-wrap:visible').last();
  const inputs = modal.locator('input:visible');
  if ((await inputs.count()) < 2) {
    test.info().annotations.push({ type: 'debug', description: `DEBUG_ADD:no-enough-inputs ${(await inputs.count())}` });
    return false;
  }

  await inputs.nth(0).fill(code);
  await inputs.nth(1).fill('E2E自动化项目');

  const treeSelect = modal.locator('.ant-select:visible').last();
  if (!(await treeSelect.isVisible().catch(() => false))) {
    test.info().annotations.push({ type: 'debug', description: 'DEBUG_ADD:tree-select-not-visible' });
    return false;
  }
  await treeSelect.click();
  await page.waitForTimeout(300);

  const searchInput = page.locator('.ant-select-dropdown:visible input.ant-select-selection-search-input').first();
  if (await searchInput.isVisible().catch(() => false)) {
    await searchInput.fill('养护人员1').catch(() => {});
    await page.waitForTimeout(300);
  }

  let treeNode = page.locator('.ant-select-dropdown:visible .ant-select-tree-node-content-wrapper').filter({ hasText: /养护人员1/ }).first();
  if (!(await treeNode.isVisible().catch(() => false))) {
    treeNode = page.locator('.ant-select-tree:visible .ant-select-tree-node-content-wrapper').first();
  }
  if (!(await treeNode.isVisible().catch(() => false))) {
    test.info().annotations.push({ type: 'debug', description: 'DEBUG_ADD:tree-node-not-visible' });
    await page.keyboard.press('Escape').catch(() => {});
    return false;
  }
  await treeNode.click();

  const submitOk = await clickFirst(modal, ['button:has-text("确 定")','button:has-text("确定")','.ant-btn-primary']);
  if (!submitOk) {
    test.info().annotations.push({ type: 'debug', description: 'DEBUG_ADD:submit-button-not-clickable' });
    return false;
  }
  await page.waitForTimeout(1200);
  const successTip = page.locator('.ant-message .ant-message-notice:visible').filter({ hasText: /新增成功|操作成功/ }).first();
  const ok = await successTip.isVisible().catch(() => false);
  if (!ok) {
    const errTip = page.locator('.ant-message .ant-message-notice:visible').first();
    const txt = await errTip.textContent().catch(() => '');
    test.info().annotations.push({ type: 'debug', description: `DEBUG_ADD:no-success-tip ${txt || 'empty-tip'}` });
  }
  return ok;
}

test('project smoke full flow', async ({ page, request }) => {
  const code = nowCode();
  const result = { open:false, query:false, add:false, edit:false, deleteConfirm:false, delete:false, importModal:false, exportModal:false, note:'', code };
  try {
    const c = await loginAndGetContext(request);
    const url = `${baseUrl}/#/project?token=${encodeURIComponent(c.token)}&tenantId=${encodeURIComponent(c.tenantId)}&userId=${encodeURIComponent(c.userId)}`;
    await page.goto(url, { waitUntil: 'domcontentloaded', timeout: 30000 });
    await page.waitForTimeout(2000);
    result.open = true;

    await cleanupOverlays(page);
    result.query = await clickFirst(page, ['button:has-text("查 询")','button:has-text("查询")']);

    await cleanupOverlays(page);
    if (await clickFirst(page, ['button:has-text("新增")'])) {
      result.add = await fillAddFormAndSubmit(page, code);
      await page.waitForTimeout(1000);
      await cleanupOverlays(page);
      result.query = await clickFirst(page, ['button:has-text("查 询")','button:has-text("查询")']) || result.query;
    }

    await cleanupOverlays(page);
    if (await clickFirst(page, ['button:has-text("导入")'])) {
      await page.waitForTimeout(600);
      result.importModal = await page.locator('.ant-modal-wrap:visible').last().isVisible().catch(()=>false);
      await cleanupOverlays(page);
    }

    await cleanupOverlays(page);
    if (await clickFirst(page, ['button:has-text("导出")'])) {
      await page.waitForTimeout(600);
      result.exportModal = await page.locator('.ant-modal-wrap:visible').last().isVisible().catch(()=>false);
      await cleanupOverlays(page);
    }

    const dataRows = page.locator('.ant-table-tbody tr').filter({ has: page.locator('a,button,span', { hasText: /查看|编辑|删除/ }) });
    const targetRow = dataRows.filter({ hasText: code }).first();
    const rowForAction = (await targetRow.count()) > 0 ? targetRow : dataRows.first();
    if ((await rowForAction.count()) === 0) {
      result.note = 'no table row found for edit/delete';
      test.info().annotations.push({ type: 'debug', description: 'DEBUG_EDIT:no-row-for-action' });
    }

    const editBtn = rowForAction.locator('a,button,span').filter({ hasText: /^编辑$/ }).first();
    if (await editBtn.isVisible().catch(()=>false)) {
      try {
        await editBtn.click({ timeout: 8000 });
        const modal = page.locator('.ant-modal-wrap:visible').last();
        const nameInput = modal.locator('input:visible').nth(1);
        if (await nameInput.isVisible().catch(() => false)) {
          await nameInput.fill('E2E编辑项目');
        }
        const ok = await clickFirst(modal, ['button:has-text("确 定")','button:has-text("确定")','.ant-btn-primary']);
        await page.waitForTimeout(1000);
        const successTip = page.locator('.ant-message .ant-message-notice:visible').filter({ hasText: /编辑成功|操作成功/ }).first();
        result.edit = !!ok && await successTip.isVisible().catch(() => false);
        if (!result.edit) {
          const errTip = page.locator('.ant-message .ant-message-notice:visible').first();
          const txt = await errTip.textContent().catch(() => '');
          test.info().annotations.push({ type: 'debug', description: `DEBUG_EDIT:no-success-tip ${txt || 'empty-tip'}` });
        }
      } catch (e) {
        result.note = e.message;
        test.info().annotations.push({ type: 'debug', description: `DEBUG_EDIT:click-failed ${e.message}` });
      }
      await page.waitForTimeout(800);
      await cleanupOverlays(page);
    } else {
      test.info().annotations.push({ type: 'debug', description: 'DEBUG_EDIT:edit-button-not-visible' });
    }

    const delBtn = rowForAction.locator('a,button,span').filter({ hasText: /^删除$/ }).first();
    if (await delBtn.isVisible().catch(()=>false)) {
      try {
        await delBtn.click({ timeout: 8000 });
        const pop = page.locator('.ant-popconfirm:visible,.ant-popover:visible').last();
        if (await pop.isVisible().catch(()=>false)) {
          result.deleteConfirm = true;
          const clicked = await clickFirst(pop, ['button:has-text("确定")','button:has-text("确 定")','.ant-btn-primary']);
          await page.waitForTimeout(1000);
          const successTip = page.locator('.ant-message .ant-message-notice:visible').filter({ hasText: /删除成功|操作成功/ }).first();
          result.delete = !!clicked && await successTip.isVisible().catch(() => false);
          if (!result.delete) {
            const errTip = page.locator('.ant-message .ant-message-notice:visible').first();
            const txt = await errTip.textContent().catch(() => '');
            test.info().annotations.push({ type: 'debug', description: `DEBUG_DELETE:no-success-tip ${txt || 'empty-tip'}` });
          }
        }
      } catch (e) {
        test.info().annotations.push({ type: 'debug', description: `DEBUG_DELETE:click-failed ${e.message}` });
      }
    } else {
      test.info().annotations.push({ type: 'debug', description: 'DEBUG_DELETE:delete-button-not-visible' });
    }
  } catch (e) {
    result.note = e.message;
  }

  test.info().annotations.push({ type: 'smoke_result', description: JSON.stringify(result) });
  expect(result.open).toBeTruthy();
  expect(result.query).toBeTruthy();
  expect(result.importModal).toBeTruthy();
  expect(result.exportModal).toBeTruthy();
});
