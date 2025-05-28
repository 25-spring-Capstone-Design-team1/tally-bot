export default async function handler(req, res) {
  if (req.method !== 'PUT') {
    return res.status(405).json({ error: 'Method Not Allowed' });
  }

  const body = req.body;

  if (!body || !body.calculateId || !body.field) {
    return res.status(400).json({ error: 'Missing required fields in request body' });
  }

  const url = `http://tally-bot-web-backend-alb-243058276.ap-northeast-2.elb.amazonaws.com/api/update/settlement`;

  try {
    const backendRes = await fetch(url, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    });

    const text = await backendRes.text();
res.status(backendRes.status).send(text);
  } catch (error) {
    res.status(500).json({ error: 'Proxy failed', detail: error.message });
  }
}