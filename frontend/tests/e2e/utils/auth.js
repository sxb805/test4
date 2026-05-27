const crypto = require('node:crypto');

const USERNAME = process.env.E2E_USERNAME || 'zhsw_test';
const PASSWORD = process.env.E2E_PASSWORD || 'Vortex2026Info';
const BASE = process.env.E2E_BASE_URL || process.env.E2E_BASE || 'http://127.0.0.1:8000';

function md5(text) {
  return crypto.createHash('md5').update(text).digest('hex');
}

async function loginAndBuildHashUrl(request, hashPath) {
  const res = await request.post(`${BASE}/cas/login`, {
    data: {
      username: USERNAME,
      password: md5(PASSWORD),
    },
  });
  if (!res.ok()) {
    throw new Error(`login failed with status=${res.status()}`);
  }

  const tokenHeader = res.headers()['access-token'];
  if (!tokenHeader) {
    throw new Error('missing access-token header');
  }

  let parsed;
  try {
    parsed = JSON.parse(tokenHeader);
  } catch (error) {
    throw new Error(`invalid access-token header: ${error.message}`);
  }
  const token = parsed.access_token;
  if (!token) {
    throw new Error('missing access_token in access-token header');
  }

  const body = await res.json();
  if (body?.result !== 0) {
    throw new Error(`login business failed: result=${body?.result}, msg=${body?.msg || ''}`);
  }

  const tenantId = body?.data?.tenantId;
  const userId = body?.data?.userId;
  if (!tenantId || !userId) {
    throw new Error('missing tenantId/userId in login response');
  }

  const query = `token=${encodeURIComponent(token)}&tenantId=${encodeURIComponent(tenantId)}&userId=${encodeURIComponent(userId)}`;
  return `${BASE}/#/${hashPath}?${query}`;
}

module.exports = {
  loginAndBuildHashUrl,
};
