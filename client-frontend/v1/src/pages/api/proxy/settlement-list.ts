export default async function handler(req, res) {
  const { groupId } = req.query;

  if (!groupId) {
    return res.status(400).json({ error: 'Missing groupId' });
  }

  const url = `http://tally-bot-web-backend-alb-243058276.ap-northeast-2.elb.amazonaws.com/api/group/${groupId}/calculates`;

  try {
    const response = await fetch(url);
    const data = await response.json();
    res.status(response.status).json(data);
  } catch (err) {
    res.status(500).json({ error: 'Proxy failed', detail: err.message });
  }
}