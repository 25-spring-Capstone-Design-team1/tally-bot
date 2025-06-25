export default async function handler(req, res) {
  if (req.method !== 'POST') {
    return res.status(405).json({ error: 'Method Not Allowed' });
  }

  const { calculateId } = req.body;

  if (!calculateId) {
    return res.status(400).json({ error: 'Missing calculateId' });
  }

  const url = `http://tally-bot-web-backend-alb-243058276.ap-northeast-2.elb.amazonaws.com/api/calculate/complete`;

  try {
    const backendRes = await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ calculateId }),
    });

    const data = await backendRes.json();
    return res.status(backendRes.status).json(data);
  } catch (error) {
    console.error('정산 완료 프록시 실패:', error);
    return res.status(500).json({ error: 'Proxy failed', detail: error.message });
  }
}