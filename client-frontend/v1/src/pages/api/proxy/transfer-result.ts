export default async function handler(req, res) {
  const { id } = req.query;
  if (!id) return res.status(400).json({ error: 'Missing ID' });

  const url = `http://tally-bot-web-backend-alb-243058276.ap-northeast-2.elb.amazonaws.com/api/calculate/${id}/transfers`;

  try {
    const response = await fetch(url);
    const data = await response.json();
    res.status(response.status).json(data);
  } catch (err) {
    res.status(500).json({ error: 'Proxy failed', detail: err.message });
  }
}