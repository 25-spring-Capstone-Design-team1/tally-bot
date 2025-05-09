import express from 'express'
import fs from 'fs/promises'
import cors from 'cors'
import path from 'path'

const app = express()
const PORT = 4000

app.use(cors())
app.use(express.json())

app.get('/api/settlements', async (req, res) => {
  const data = await fs.readFile(path.resolve('data/settlements.json'), 'utf-8')
  res.json(JSON.parse(data))
})

app.get('/api/settlements/:id', async (req, res) => {
  const id = req.params.id
  try {
    const detail = await fs.readFile(path.resolve(`data/settlement-detail/${id}.json`), 'utf-8')
    res.json(JSON.parse(detail))
  } catch (e) {
    res.status(404).json({ error: '정산 정보를 찾을 수 없습니다' })
  }
})

app.listen(PORT, () => {
  console.log(`Mock API server running at http://localhost:${PORT}`)
})

app.put('/api/settlements/:id', async (req, res) => {
  const id = req.params.id;
  const updatedData = req.body;

  const detailPath = path.resolve(`data/settlement-detail/${id}.json`);
  const listPath = path.resolve(`data/settlements.json`);

  try {
    // 1. 상세 정산 파일 저장
    await fs.writeFile(detailPath, JSON.stringify(updatedData, null, 2));

    // 2. settlements.json 목록도 수정
    const listRaw = await fs.readFile(listPath, 'utf-8');
    const list = JSON.parse(listRaw);

    const index = list.findIndex((item) => item.id === id);
    if (index !== -1) {
      list[index] = {
        id,
        title: updatedData.title,
        createdAt: updatedData.createdAt || null,
        participantCount: updatedData.participants.length,
        totalAmount: updatedData.payments.reduce((sum, p) => sum + (p.amount || 0), 0),
        isCompleted: updatedData.isCompleted,
      };
    }

    await fs.writeFile(listPath, JSON.stringify(list, null, 2));

    res.json(updatedData);
  } catch (e) {
    console.error(`정산 ${id} 저장 실패`, e);
    res.status(500).json({ error: '정산 저장 실패' });
  }
});