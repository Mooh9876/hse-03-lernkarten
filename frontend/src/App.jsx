import { useState, useEffect } from 'react'
import './App.css'

const API = '/todos'

export default function App() {
  const [cards, setCards] = useState([])
  const [flipped, setFlipped] = useState({})
  const [learned, setLearned] = useState({})
  const [showForm, setShowForm] = useState(false)
  const [editCard, setEditCard] = useState(null)
  const [form, setForm] = useState({ title: '', description: '' })
  const [filter, setFilter] = useState('all') // all | learned | open

  useEffect(() => { fetchCards() }, [])

  async function fetchCards() {
    const res = await fetch(API)
    const data = await res.json()
    setCards(data)
    const l = {}
    data.forEach(c => { if (c.completed) l[c.id] = true })
    setLearned(l)
  }

  async function createCard() {
    if (!form.title.trim()) return
    await fetch(API, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ title: form.title, description: form.description, completed: false }),
    })
    setForm({ title: '', description: '' })
    setShowForm(false)
    fetchCards()
  }

  async function updateCard() {
    if (!form.title.trim()) return
    await fetch(`${API}/${editCard.id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ ...editCard, title: form.title, description: form.description }),
    })
    setEditCard(null)
    setForm({ title: '', description: '' })
    fetchCards()
  }

  async function deleteCard(id) {
    await fetch(`${API}/${id}`, { method: 'DELETE' })
    fetchCards()
  }

  async function toggleLearned(card) {
    const updated = { ...card, completed: !card.completed }
    await fetch(`${API}/${card.id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(updated),
    })
    setLearned(prev => ({ ...prev, [card.id]: !prev[card.id] }))
    fetchCards()
  }

  function startEdit(card) {
    setEditCard(card)
    setForm({ title: card.title, description: card.description })
    setShowForm(true)
  }

  function cancelForm() {
    setShowForm(false)
    setEditCard(null)
    setForm({ title: '', description: '' })
  }

  const filtered = cards.filter(c => {
    if (filter === 'learned') return c.completed
    if (filter === 'open') return !c.completed
    return true
  })

  const learnedCount = cards.filter(c => c.completed).length

  return (
    <div className="app">
      <header className="header">
        <div className="header-left">
          <span className="logo">🧠</span>
          <div>
            <h1>Lernkarten</h1>
            <p className="subtitle">Verteilte Systeme – HSE 2026</p>
          </div>
        </div>
        <div className="header-right">
          <div className="progress-pill">
            <span>{learnedCount}</span> / <span>{cards.length}</span> gelernt
          </div>
          <button className="btn-primary" onClick={() => { setShowForm(true); setEditCard(null) }}>
            + Neue Karte
          </button>
        </div>
      </header>

      {showForm && (
        <div className="modal-overlay" onClick={cancelForm}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <h2>{editCard ? 'Karte bearbeiten' : 'Neue Lernkarte'}</h2>
            <label>Frage / Begriff</label>
            <input
              autoFocus
              placeholder="z.B. Was ist ein verteiltes System?"
              value={form.title}
              onChange={e => setForm(f => ({ ...f, title: e.target.value }))}
              onKeyDown={e => e.key === 'Enter' && (editCard ? updateCard() : createCard())}
            />
            <label>Antwort / Erklärung</label>
            <textarea
              rows={4}
              placeholder="z.B. Ein System aus mehreren unabhängigen Prozessen..."
              value={form.description}
              onChange={e => setForm(f => ({ ...f, description: e.target.value }))}
            />
            <div className="modal-actions">
              <button className="btn-ghost" onClick={cancelForm}>Abbrechen</button>
              <button className="btn-primary" onClick={editCard ? updateCard : createCard}>
                {editCard ? 'Speichern' : 'Erstellen'}
              </button>
            </div>
          </div>
        </div>
      )}

      <div className="toolbar">
        {['all', 'open', 'learned'].map(f => (
          <button
            key={f}
            className={`filter-btn ${filter === f ? 'active' : ''}`}
            onClick={() => setFilter(f)}
          >
            {f === 'all' ? `Alle (${cards.length})` : f === 'open' ? `Offen (${cards.length - learnedCount})` : `Gelernt (${learnedCount})`}
          </button>
        ))}
      </div>

      {filtered.length === 0 ? (
        <div className="empty">
          <span>📭</span>
          <p>Keine Karten gefunden.</p>
          <button className="btn-primary" onClick={() => setShowForm(true)}>Erste Karte erstellen</button>
        </div>
      ) : (
        <div className="grid">
          {filtered.map(card => (
            <div
              key={card.id}
              className={`card ${flipped[card.id] ? 'flipped' : ''} ${card.completed ? 'done' : ''}`}
              onClick={() => setFlipped(f => ({ ...f, [card.id]: !f[card.id] }))}
            >
              <div className="card-inner">
                <div className="card-front">
                  {card.completed && <span className="badge">✓ Gelernt</span>}
                  <div className="card-number">#{card.id}</div>
                  <p className="card-question">{card.title}</p>
                  <span className="flip-hint">Klicken zum Umdrehen →</span>
                </div>
                <div className="card-back">
                  <p className="card-answer">{card.description || '(keine Antwort)'}</p>
                  <span className="flip-hint">← Zurückdrehen</span>
                </div>
              </div>
              <div className="card-actions" onClick={e => e.stopPropagation()}>
                <button
                  className={`btn-learn ${card.completed ? 'unlearn' : ''}`}
                  onClick={() => toggleLearned(card)}
                  title={card.completed ? 'Als nicht gelernt markieren' : 'Als gelernt markieren'}
                >
                  {card.completed ? '↩ Wiederholen' : '✓ Gelernt'}
                </button>
                <button className="btn-icon" onClick={() => startEdit(card)} title="Bearbeiten">✏️</button>
                <button className="btn-icon danger" onClick={() => deleteCard(card.id)} title="Löschen">🗑️</button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
