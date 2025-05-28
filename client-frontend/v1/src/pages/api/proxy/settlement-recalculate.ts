export const config = {
  api: {
    bodyParser: true,
  },
};

export default async function handler(req, res) {
  if (req.method !== 'POST') {
    return res.status(405).json({ error: 'Method Not Allowed' });
  }

  const { calculateId } = req.body;

  if (!calculateId) {
    console.error('❗ calculateId 누락됨. 실제 body:', req.body); // debug용
    return res.status(400).json({ error: 'Missing calculateId in request body' });
  }

  const url = `http://tally-bot-web-backend-alb-243058276.ap-northeast-2.elb.amazonaws.com/api/calculate/recalculate`;

  try {
    const backendRes = await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ calculateId }),
    });

    const data = await backendRes.json();
    res.status(backendRes.status).json(data);
  } catch (err) {
    res.status(500).json({ error: 'Proxy failed', detail: err.message });
  }
}