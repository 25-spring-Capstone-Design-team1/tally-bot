export default async function handler(req, res) {
  const { id } = req.query;

  if (!id) {
    return res.status(400).json({ error: 'Missing settlement ID' });
  }

  const url = `http://52.79.167.2:8080/api/calculate/${id}/settlements`;

  try {
    const response = await fetch(url);
    const data = await response.json();
    res.status(response.status).json(data);
  } catch (err) {
    res.status(500).json({ error: 'Proxy failed', detail: err.message });
  }
}