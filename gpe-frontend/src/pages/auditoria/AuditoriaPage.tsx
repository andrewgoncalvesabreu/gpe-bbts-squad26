import { useEffect, useState, type FormEvent } from 'react'
import { Button } from 'primereact/button'
import { Column } from 'primereact/column'
import { DataTable } from 'primereact/datatable'
import { Dialog } from 'primereact/dialog'
import { Dropdown } from 'primereact/dropdown'
import { InputText } from 'primereact/inputtext'
import type { Auditoria } from '../../types/auditoria'
import { auditoriaService, type AuditoriaFiltros } from '../../services/auditoriaService'
import { useToast } from '../../components/common/toastContext'
import { parseApiError } from '../../utils/apiError'
import { formatDateTime } from '../../utils/format'

const operacoes = [
  'CREATE',
  'UPDATE',
  'DELETE',
  'ADD_VERSION',
  'DOWNLOAD',
  'SUBMIT_FOR_SIGNATURE',
  'SIGNATURE_FAILURE',
  'EVALUATE',
  'SUBMIT_FOR_APPROVAL',
  'APPROVE',
  'REJECT',
  'EXTEND_VALIDITY',
]

const entidades = ['Documento', 'InstrumentoJuridico', 'Oportunidade', 'Parceiro']

function snapshot(value?: string | null) {
  if (!value) return '—'
  try {
    return JSON.stringify(JSON.parse(value), null, 2)
  } catch {
    return value
  }
}

export default function AuditoriaPage() {
  const toast = useToast()
  const [registros, setRegistros] = useState<Auditoria[]>([])
  const [loading, setLoading] = useState(false)
  const [usuario, setUsuario] = useState('')
  const [operacao, setOperacao] = useState<string | null>(null)
  const [entidade, setEntidade] = useState<string | null>(null)
  const [inicio, setInicio] = useState('')
  const [fim, setFim] = useState('')
  const [selecionado, setSelecionado] = useState<Auditoria | null>(null)

  const carregar = async (filtros: AuditoriaFiltros = {}) => {
    setLoading(true)
    try {
      setRegistros(await auditoriaService.consultar(filtros))
    } catch (error) {
      toast.error(parseApiError(error).message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { void carregar() }, [])

  const consultar = (event?: FormEvent) => {
    event?.preventDefault()
    void carregar({
      usuario: usuario.trim() || undefined,
      operacao: operacao || undefined,
      entidade: entidade || undefined,
      inicio: inicio ? new Date(inicio).toISOString() : undefined,
      fim: fim ? new Date(fim).toISOString() : undefined,
    })
  }

  const limpar = () => {
    setUsuario(''); setOperacao(null); setEntidade(null); setInicio(''); setFim('')
    void carregar()
  }

  return (
    <div className="flex flex-col gap-5">
      <div>
        <h1 className="text-2xl font-bold">Trilha de auditoria</h1>
        <p className="mt-1 text-sm text-zinc-500">Consulte as operações realizadas no SIGES-PES/GPE.</p>
      </div>

      <form onSubmit={consultar} className="rounded-xl border border-zinc-200 bg-white p-4">
        <div className="grid gap-3 md:grid-cols-2 lg:grid-cols-5">
          <InputText value={usuario} onChange={(event) => setUsuario(event.target.value)} placeholder="Usuário" />
          <Dropdown value={operacao} options={operacoes} onChange={(event) => setOperacao(event.value)} placeholder="Operação" showClear />
          <Dropdown value={entidade} options={entidades} onChange={(event) => setEntidade(event.value)} placeholder="Entidade" showClear />
          <div className="flex flex-col gap-1"><label className="text-xs text-zinc-500">A partir de</label><input className="rounded-md border border-zinc-300 px-3 py-2 text-sm" type="datetime-local" value={inicio} onChange={(event) => setInicio(event.target.value)} /></div>
          <div className="flex flex-col gap-1"><label className="text-xs text-zinc-500">Até</label><input className="rounded-md border border-zinc-300 px-3 py-2 text-sm" type="datetime-local" value={fim} onChange={(event) => setFim(event.target.value)} /></div>
        </div>
        <div className="mt-4 flex justify-end gap-2"><Button type="button" label="Limpar" text onClick={limpar} /><Button type="submit" icon="pi pi-search" label="Consultar" loading={loading} /></div>
      </form>

      <div className="rounded-xl border border-zinc-200 bg-white p-2">
        <DataTable value={registros} loading={loading} dataKey="id" paginator rows={15} emptyMessage="Nenhum registro encontrado." sortField="dataHora" sortOrder={-1}>
          <Column field="dataHora" header="Data/hora" body={(item: Auditoria) => formatDateTime(item.dataHora)} sortable />
          <Column field="usuario" header="Usuário" sortable />
          <Column field="operacao" header="Operação" sortable />
          <Column field="entidade" header="Entidade" sortable />
          <Column field="entidadeId" header="ID da entidade" />
          <Column header="Detalhes" body={(item: Auditoria) => <Button icon="pi pi-eye" label="Visualizar" text onClick={() => setSelecionado(item)} />} />
        </DataTable>
      </div>

      <Dialog header="Detalhes da auditoria" visible={selecionado !== null} onHide={() => setSelecionado(null)} style={{ width: '58rem', maxWidth: '95vw' }}>
        {selecionado && <div className="flex flex-col gap-4 text-sm">
          <div className="grid gap-3 md:grid-cols-2"><div><strong>Usuário:</strong> {selecionado.usuario}</div><div><strong>Data/hora:</strong> {formatDateTime(selecionado.dataHora)}</div><div><strong>Operação:</strong> {selecionado.operacao}</div><div><strong>Entidade:</strong> {selecionado.entidade}</div><div className="md:col-span-2"><strong>ID:</strong> {selecionado.entidadeId || '—'}</div></div>
          <div><h3 className="mb-1 font-semibold">Conteúdo anterior</h3><pre className="max-h-56 overflow-auto rounded-lg bg-zinc-50 p-3 text-xs">{snapshot(selecionado.conteudoAnterior)}</pre></div>
          <div><h3 className="mb-1 font-semibold">Conteúdo novo</h3><pre className="max-h-56 overflow-auto rounded-lg bg-zinc-50 p-3 text-xs">{snapshot(selecionado.conteudoNovo)}</pre></div>
        </div>}
      </Dialog>
    </div>
  )
}
