import { useCallback, useEffect, useMemo, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { Button } from 'primereact/button'
import { ConfirmDialog, confirmDialog } from 'primereact/confirmdialog'
import { Dialog } from 'primereact/dialog'
import { Dropdown } from 'primereact/dropdown'
import { InputNumber } from 'primereact/inputnumber'
import { InputTextarea } from 'primereact/inputtextarea'
import { InputText } from 'primereact/inputtext'
import { TabPanel, TabView } from 'primereact/tabview'
import { useToast } from '../../components/common/toastContext'
import { AVALIACAO_CRITERIOS, STATUS_OPORTUNIDADE_OPTIONS, labelOrigemOportunidade } from '../../constants/oportunidade'
import { oportunidadeService } from '../../services/oportunidadeService'
import type { AvaliacaoOportunidade, AvaliacaoPayload, Oportunidade, StatusOportunidade } from '../../types/oportunidade'
import { parseApiError } from '../../utils/apiError'
import { formatDate, formatDateTime, formatMoney } from '../../utils/format'
import { OportunidadeStatusTag } from './components/OportunidadeStatusTag'

const initialEvaluation: AvaliacaoPayload = { alinhamentoEstrategico: 0, potencialInovacao: 0, retornoFinanceiro: 0, impactoInstitucional: 0, viabilidadeTecnica: 0, parecerTecnico: '', avaliadorMatricula: '' }
function DetailRow({ label, value }: { label: string; value?: string | null }) { return <div className="py-2"><dt className="text-xs uppercase tracking-wide text-zinc-400">{label}</dt><dd className="whitespace-pre-wrap text-sm text-zinc-900">{value || '—'}</dd></div> }
function average(item: AvaliacaoOportunidade) { return (item.alinhamentoEstrategico + item.potencialInovacao + item.retornoFinanceiro + item.impactoInstitucional + item.viabilidadeTecnica) / 5 }

export default function OportunidadeDetailPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const toast = useToast()
  const [oportunidade, setOportunidade] = useState<Oportunidade | null>(null)
  const [avaliacoes, setAvaliacoes] = useState<AvaliacaoOportunidade[]>([])
  const [loading, setLoading] = useState(true)
  const [dialogOpen, setDialogOpen] = useState(false)
  const [evaluation, setEvaluation] = useState<AvaliacaoPayload>(initialEvaluation)
  const [saving, setSaving] = useState(false)
  const [newStatus, setNewStatus] = useState<StatusOportunidade | null>(null)

  const carregar = useCallback(async () => {
    if (!id) return
    setLoading(true)
    try { const [item, history] = await Promise.all([oportunidadeService.buscarPorId(id), oportunidadeService.listarAvaliacoes(id)]); setOportunidade(item); setAvaliacoes(history) }
    catch (err) { toast.error(parseApiError(err).message); navigate('/oportunidades', { replace: true }) }
    finally { setLoading(false) }
  }, [id, navigate, toast])
  useEffect(() => { void carregar() }, [carregar])

  const updateEvaluation = <K extends keyof AvaliacaoPayload>(key: K, value: AvaliacaoPayload[K]) => setEvaluation((previous) => ({ ...previous, [key]: value }))

  const alterarStatus = async (status: StatusOportunidade | null) => {
    if (!id || !status || !oportunidade || status === oportunidade.statusWorkflow) return
    try { const updated = await oportunidadeService.alterarStatus(id, status); setOportunidade(updated); setNewStatus(null); toast.success('Status atualizado com sucesso.') }
    catch (err) { toast.error(parseApiError(err).message) }
  }
  const excluir = () => confirmDialog({ message: `Cancelar a oportunidade "${oportunidade?.titulo}"?`, header: 'Confirmar cancelamento', icon: 'pi pi-exclamation-triangle', acceptLabel: 'Cancelar oportunidade', rejectLabel: 'Voltar', acceptClassName: 'p-button-danger', accept: async () => { if (!id) return; try { await oportunidadeService.excluir(id); toast.success('Oportunidade cancelada.'); navigate('/oportunidades') } catch (err) { toast.error(parseApiError(err).message) } } })
  const salvarAvaliacao = async () => {
    if (!id || Object.values(evaluation).slice(0, 5).some((value) => !value) || !evaluation.avaliadorMatricula.trim()) { toast.error('Informe uma nota de 1 a 5 para todos os critérios e a matrícula do avaliador.'); return }
    setSaving(true)
    try { await oportunidadeService.avaliar(id, { ...evaluation, avaliadorMatricula: evaluation.avaliadorMatricula.trim(), parecerTecnico: evaluation.parecerTecnico?.trim() || null }); toast.success('Avaliação registrada.'); setDialogOpen(false); setEvaluation(initialEvaluation); await carregar() }
    catch (err) { toast.error(parseApiError(err).message) } finally { setSaving(false) }
  }
  const ultimaAvaliacao = avaliacoes[0]
  const media = useMemo(() => ultimaAvaliacao ? average(ultimaAvaliacao).toFixed(1) : '—', [ultimaAvaliacao])
  if (loading) return <p className="text-zinc-500">Carregando oportunidade...</p>
  if (!oportunidade) return null
  return <div className="flex flex-col gap-5">
    <ConfirmDialog />
    <div className="flex flex-wrap items-start justify-between gap-4 rounded-xl border border-zinc-200 bg-white p-5"><div><div className="mb-2 flex flex-wrap items-center gap-3"><h1 className="text-xl font-bold">{oportunidade.titulo}</h1><OportunidadeStatusTag status={oportunidade.statusWorkflow} /></div><p className="text-sm text-zinc-500">{oportunidade.parceiro?.razaoSocial} · criada em {formatDate(oportunidade.dataCriacao)}</p></div><div className="flex flex-wrap gap-2"><Button type="button" icon="pi pi-arrow-left" label="Voltar" outlined onClick={() => navigate('/oportunidades')} /><Button type="button" icon="pi pi-pencil" label="Editar" onClick={() => navigate(`/oportunidades/${oportunidade.id}/editar`)} /><Button type="button" icon="pi pi-times" label="Cancelar" severity="danger" outlined onClick={excluir} /></div></div>
    <div className="grid grid-cols-1 gap-4 lg:grid-cols-3"><section className="rounded-xl border border-zinc-200 bg-white p-5"><h2 className="mb-3 text-base font-semibold">Fluxo</h2><Dropdown value={newStatus ?? oportunidade.statusWorkflow} options={STATUS_OPORTUNIDADE_OPTIONS} onChange={(event) => { setNewStatus(event.value); void alterarStatus(event.value) }} className="w-full" /><p className="mt-2 text-xs text-zinc-500">A alteração é enviada diretamente ao workflow da oportunidade.</p></section><section className="rounded-xl border border-zinc-200 bg-white p-5"><h2 className="mb-3 text-base font-semibold">Avaliação mais recente</h2><div className="text-3xl font-bold text-zinc-800">{media}<span className="text-sm font-normal text-zinc-400"> / 5</span></div><p className="mt-1 text-sm text-zinc-500">{ultimaAvaliacao ? `Avaliada em ${formatDateTime(ultimaAvaliacao.dataAvaliacao)}` : 'Ainda não avaliada'}</p></section><section className="rounded-xl border border-zinc-200 bg-white p-5"><h2 className="mb-3 text-base font-semibold">Estimativa financeira</h2><div className="text-2xl font-bold text-zinc-800">{formatMoney(oportunidade.estimativaFinanceira)}</div><p className="mt-1 text-sm text-zinc-500">Responsável: {oportunidade.responsavelMatricula}</p></section></div>
    <TabView><TabPanel header="Detalhamento"><div className="grid grid-cols-1 gap-4 lg:grid-cols-2"><section className="rounded-xl border border-zinc-200 bg-white p-5"><h2 className="mb-2 text-base font-semibold">Informações da oportunidade</h2><dl><DetailRow label="Origem" value={labelOrigemOportunidade(oportunidade.origem)} /><DetailRow label="Área demandante" value={oportunidade.areaDemandante} /><DetailRow label="Descrição" value={oportunidade.descricao} /><DetailRow label="Objetivos" value={oportunidade.objetivos} /></dl></section><section className="rounded-xl border border-zinc-200 bg-white p-5"><h2 className="mb-2 text-base font-semibold">Justificativa e benefícios</h2><dl><DetailRow label="Justificativa" value={oportunidade.justificativa} /><DetailRow label="Benefícios esperados" value={oportunidade.beneficiosEsperados} /><DetailRow label="Problema demandado" value={oportunidade.problemaDemandado} /><DetailRow label="Solução proposta" value={oportunidade.solucaoProposta} /></dl></section></div></TabPanel><TabPanel header={`Avaliações (${avaliacoes.length})`}><div className="mb-4 flex justify-end"><Button type="button" icon="pi pi-plus" label="Registrar avaliação" onClick={() => setDialogOpen(true)} /></div>{avaliacoes.length === 0 ? <p className="rounded-xl border border-zinc-200 bg-white p-5 text-sm text-zinc-500">Nenhuma avaliação registrada.</p> : <div className="flex flex-col gap-3">{avaliacoes.map((item) => <article key={item.id} className="rounded-xl border border-zinc-200 bg-white p-5"><div className="flex flex-wrap items-center justify-between gap-2"><div><strong>Nota média: {average(item).toFixed(1)} / 5</strong><p className="text-xs text-zinc-500">{item.avaliadorMatricula} · {formatDateTime(item.dataAvaliacao)}</p></div></div><div className="mt-3 grid grid-cols-1 gap-2 text-sm md:grid-cols-5">{AVALIACAO_CRITERIOS.map((criterio) => <div key={criterio.key} className="rounded-lg bg-zinc-50 p-2"><div className="text-xs text-zinc-500">{criterio.label}</div><strong>{item[criterio.key]} / 5</strong></div>)}</div>{item.parecerTecnico && <p className="mt-3 whitespace-pre-wrap text-sm text-zinc-700">{item.parecerTecnico}</p>}</article>)}</div>}</TabPanel></TabView>
    <Dialog header="Registrar avaliação" visible={dialogOpen} onHide={() => setDialogOpen(false)} style={{ width: '42rem', maxWidth: '95vw' }} footer={<div className="flex justify-end gap-2"><Button type="button" label="Cancelar" outlined onClick={() => setDialogOpen(false)} /><Button type="button" label="Salvar avaliação" loading={saving} onClick={salvarAvaliacao} /></div>}><div className="flex flex-col gap-4">{AVALIACAO_CRITERIOS.map((criterio) => <div key={criterio.key} className="flex items-center justify-between gap-4"><label htmlFor={`avaliacao-${criterio.key}`} className="text-sm font-medium">{criterio.label}</label><InputNumber id={`avaliacao-${criterio.key}`} value={evaluation[criterio.key]} onValueChange={(event) => updateEvaluation(criterio.key, event.value ?? 0)} min={1} max={5} showButtons /></div>)}<div className="flex flex-col gap-1"><label htmlFor="avaliador-matricula" className="text-sm font-medium">Matrícula do avaliador</label><InputText id="avaliador-matricula" value={evaluation.avaliadorMatricula} onChange={(event) => updateEvaluation('avaliadorMatricula', event.target.value)} /></div><div className="flex flex-col gap-1"><label htmlFor="parecer-tecnico" className="text-sm font-medium">Parecer técnico</label><InputTextarea id="parecer-tecnico" rows={4} value={evaluation.parecerTecnico ?? ''} onChange={(event) => updateEvaluation('parecerTecnico', event.target.value)} /></div></div></Dialog>
  </div>
}
