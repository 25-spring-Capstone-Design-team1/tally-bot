export default async function handler(req, res) {
  const { groupId } = req.query;

  if (!groupId) {
    return res.status(400).json({ error: 'Missing groupId' });
  }

  const apiUrl = `http://tally-bot-web-backend-alb-243058276.ap-northeast-2.elb.amazonaws.com/api/group/${groupId}/members`;

  try {
    const response = await fetch(apiUrl);
    const data = await response.json();
    res.status(response.status).json(data);
  } catch (err) {
    res.status(500).json({ error: 'Proxy failed', detail: err.message });
  }
}