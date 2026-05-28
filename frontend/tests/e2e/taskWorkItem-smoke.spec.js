const { test, expect } = require('@playwright/test');
const { loginAndBuildHashUrl } = require('./utils/auth');

test('@smoke taskWorkItem key path smoke', async ({ page, request }) => {
  test.setTimeout(120000);
  const url = await loginAndBuildHashUrl(request, 'taskWorkItem');
  await page.goto(url, { waitUntil: 'domcontentloaded' });

  await expect(page.locator('text=任务工单管理').first()).toBeVisible({ timeout: 30000 });

  const queryBtn = page.getByRole('button', { name: /查\s*询/ }).first();
  await queryBtn.click();

  const addBtn = page.getByRole('button', { name: /新\s*增/ }).first();
  await addBtn.click();
  const visibleModal = page.locator('.ant-modal-wrap:visible').last();
  await expect(visibleModal).toBeVisible();
  await visibleModal.getByRole('button', { name: /取\s*消|关\s*闭/ }).first().click();

  const importBtn = page.getByRole('button', { name: /导\s*入/ }).first();
  await importBtn.click();
  await expect(page.locator('.ant-modal-wrap:visible').last()).toBeVisible();
  await page.getByRole('button', { name: /关\s*闭/ }).first().click();

  const exportBtn = page.getByRole('button', { name: /导\s*出/ }).first();
  await exportBtn.click();
  await expect(page.locator('.ant-modal-wrap:visible').last()).toBeVisible();
  await page.getByRole('button', { name: /取\s*消|关\s*闭/ }).first().click();
});

test('@smoke taskWorkItem occupancy page smoke', async ({ page, request }) => {
  test.setTimeout(120000);
  const url = await loginAndBuildHashUrl(request, 'taskWorkItem/occupancy');
  await page.goto(url, { waitUntil: 'domcontentloaded' });
  await expect(page.locator('text=人员资源占用表').first()).toBeVisible({ timeout: 30000 });
  await page.getByRole('button', { name: /导\s*出/ }).first().click();
  await expect(page.locator('.ant-modal-wrap:visible').last()).toBeVisible();
  await page.getByRole('button', { name: /取\s*消|关\s*闭/ }).first().click();
  await page.getByRole('button', { name: /查\s*询/ }).first().click();
  await page.getByRole('button', { name: /重\s*置/ }).first().click();
});

test('@smoke taskWorkItem project occupancy page smoke', async ({ page, request }) => {
  test.setTimeout(120000);
  const url = await loginAndBuildHashUrl(request, 'taskWorkItem/projectOccupancy');
  await page.goto(url, { waitUntil: 'domcontentloaded' });
  await expect(page.locator('text=项目资源占用表').first()).toBeVisible({ timeout: 30000 });
  await page.getByRole('button', { name: /导\s*出/ }).first().click();
  await expect(page.locator('.ant-modal-wrap:visible').last()).toBeVisible();
  await page.getByRole('button', { name: /取\s*消|关\s*闭/ }).first().click();
  await page.getByRole('button', { name: /查\s*询/ }).first().click();
  await page.getByRole('button', { name: /重\s*置/ }).first().click();
});
