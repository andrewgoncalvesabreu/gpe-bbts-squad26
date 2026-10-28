import { useEffect, useState, type FormEvent } from 'react'
import { Button } from 'primereact/button'
import { Column } from 'primereact/column'
import { DataTable } from 'primereact/datatable'
import { Dialog } from 'primereact/dialog'
import { InputText } from 'primereact/inputtext'
import { InputTextarea } from 'primereact/inputtextarea'
import { Dropdown } from 'primereact/dropdown'
import { documentoService } from '../../services/documentoService'
import type { Documento, DocumentoVersao } from '../../types/documento'
import { parceiroService } from '../../services/parceiroService'
import type { Parceiro } from '../../types/parceiro'
import { useToast } from '../../components/common/toastContext'
import { parseApiError } from '../../utils/apiError'
import { formatDateTime } from '../../utils/format'

const tamanho = (bytes?: number | null) => {
  if (!bytes) return '—'
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}

export default function DocumentosPage() {
  const toast = useToast()
  const [documentos, setDocumentos] = useState<Documento[]>([])
  const [loading, setLoading] = useState(true)
  const [dialogOpen, setDialogOpen] = useState(false)
  const [versoesOpen, setVersoesOpen] = useState(false)
  const [documentoSelecionado, setDocumentoSelecionado] = useState<Documento | null>(null)
  const [versoes, setVersoes] = useState<DocumentoVersao[]>([])
  const [nome, setNome] = useState('')
  const [descricao, setDescricao] = useState('')
  const [arquivo, setArquivo] = useState<File | null>(null)
  const [parceiros, setParceiros] = useState<Parceiro[]>([])
  const [parceiroId, setParceiroId] = useState('')
  const [saving, setSaving] = useState(false)
  const [novaVersao, setNovaVersao] = useState<File | null>(null)

  const carregar = () => {
    setLoading(true)
    documentoService.listar().then(setDocumentos).catch((e) => toast.error(parseApiError(e).message)).finally(() => setLoading(false))
  }
  useEffect(() => { carregar(); parceiroService.listar().then(setParceiros).catch((e) => toast.error(parseApiError(e).message)) }, [])

  const salvar = async (event: FormEvent) => {
    event.preventDefault()
    if (!nome.trim() || !arquivo || !parceiroId) { toast.error('Informe o nome, o parceiro e selecione um arquivo.'); return }
    const form = new FormData()
    form.append('nome', nome.trim())
    form.append('descricao', descricao.trim())
    form.append('arquivo', arquivo)
    form.append('parceiroId', parceiroId)
    setSaving(true)
    try {
      await documentoService.criar(form)
      toast.success('Documento enviado com sucesso.')
      setDialogOpen(false); setNome(''); setDescricao(''); setArquivo(null); setParceiroId(''); carregar()
    } catch (e) { toast.error(parseApiError(e).message) } finally { setSaving(false) }
  }

  const abrirVersoes = async (documento: Documento) => {
    setDocumentoSelecionado(documento); setVersoesOpen(true)
    try { setVersoes(await documentoService.listarVersoes(documento.id)) } catch (e) { toast.error(parseApiError(e).message) }
  }
  const enviarNovaVersao = async () => {
    if (!documentoSelecionado || !novaVersao) return
    try {
      await documentoService.adicionarVersao(documentoSelecionado.id, novaVersao)
      toast.success('Nova versão enviada.')
      setNovaVersao(null); setVersoes(await documentoService.listarVersoes(documentoSelecionado.id)); carregar()
    } catch (e) { toast.error(parseApiError(e).message) }
  }

  return <div className="flex flex-col gap-5">
    <div className="flex items-center justify-between"><div><h1 className="text-2xl font-bold">Gestão Documental</h1><p className="mt-1 text-sm text-zinc-500">Arquivos vinculados às parcerias e seu histórico de versões.</p></div><Button icon="pi pi-upload" label="Enviar documento" onClick={() => setDialogOpen(true)} /></div>
    <div className="rounded-xl border border-zinc-200 bg-white p-2"><DataTable value={documentos} loading={loading} dataKey="id" paginator rows={10} emptyMessage="Nenhum documento cadastrado.">
      <Column field="nome" header="Documento" />
      <Column header="Vínculo" body={(d: Documento) => d.parceiroNome || d.oportunidadeTitulo || d.instrumentoNome || '—'} />
      <Column header="Arquivo atual" body={(d: Documento) => d.nomeArquivoAtual || '—'} />
      <Column header="Versão" body={(d: Documento) => d.versaoAtual ? `v${d.versaoAtual}` : '—'} />
      <Column header="Atualizado em" body={(d: Documento) => formatDateTime(d.dataAtualizacao)} />
      <Column header="Ações" body={(d: Documento) => <Button icon="pi pi-history" label="Versões" text onClick={() => void abrirVersoes(d)} />} />
    </DataTable></div>
    <Dialog header="Enviar documento" visible={dialogOpen} onHide={() => setDialogOpen(false)} style={{ width: '36rem', maxWidth: '95vw' }}>
      <form onSubmit={salvar} className="flex flex-col gap-4"><InputText value={nome} onChange={(e) => setNome(e.target.value)} placeholder="Nome do documento" /><Dropdown value={parceiroId || null} options={parceiros} optionLabel="razaoSocial" optionValue="id" onChange={(e) => setParceiroId(e.value ?? '')} placeholder="Selecione o parceiro" /><InputTextarea value={descricao} onChange={(e) => setDescricao(e.target.value)} placeholder="Descrição (opcional)" rows={3} /><input type="file" onChange={(e) => setArquivo(e.target.files?.[0] ?? null)} /><div className="flex justify-end"><Button type="submit" label="Enviar" loading={saving} /></div></form>
    </Dialog>
    <Dialog header={`Versões — ${documentoSelecionado?.nome ?? ''}`} visible={versoesOpen} onHide={() => setVersoesOpen(false)} style={{ width: '48rem', maxWidth: '95vw' }}>
      <div className="mb-4 flex items-center gap-2"><input type="file" onChange={(e) => setNovaVersao(e.target.files?.[0] ?? null)} /><Button label="Enviar nova versão" icon="pi pi-upload" disabled={!novaVersao} onClick={() => void enviarNovaVersao()} /></div>
      <DataTable value={versoes} dataKey="id" emptyMessage="Nenhuma versão encontrada."><Column field="numeroVersao" header="Versão" body={(v: DocumentoVersao) => `v${v.numeroVersao}`} /><Column field="nomeArquivo" header="Arquivo" /><Column header="Tamanho" body={(v: DocumentoVersao) => tamanho(v.tamanhoBytes)} /><Column header="Enviado em" body={(v: DocumentoVersao) => formatDateTime(v.dataEnvio)} /><Column header="Ação" body={(v: DocumentoVersao) => <Button icon="pi pi-download" text aria-label="Baixar" onClick={() => documentoService.baixar(documentoSelecionado!.id, v.numeroVersao, v.nomeArquivo)} />} /></DataTable>
    </Dialog>
  </div>
}