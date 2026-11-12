import { useCallback, useEffect, useMemo, useState, type FormEvent } from 'react'
import { Button } from 'primereact/button'
import { Dropdown } from 'primereact/dropdown'
import { InputText } from 'primereact/inputtext'
import { InputTextarea } from 'primereact/inputtextarea'
import { useToast } from '../../components/common/toastContext'
import { projetoService } from '../../services/projetoService'
import type {
  EntregaPlano,
  InstrumentoResumo,
  MarcoPlano,
  Parceria,
  ParceiroResumo,
  PlanoTrabalhoPayload,
  Projeto,
} from '../../types/projeto'
import { parseApiError } from '../../utils/apiError'

interface PlanoDraft {
  objetivos: string[]
  cronogramaInicio: string
  cronogramaFim: string
  responsaveis: string
  entregas: EntregaPlano[]
  marcos: MarcoPlano[]
}

const EMPTY_PLANO: PlanoDraft = {
  objetivos: [''],
  cronogramaInicio: '',
  cronogramaFim: '',
  responsaveis: '',
  entregas: [],
  marcos: [],
}

function localDate(value: Date) {
  const offset = value.getTimezoneOffset()
  return new Date(value.getTime() - offset * 60_000).toISOString().slice(0, 10)
}

export default function ProjetosPage() {
  const toast = useToast()
  const [parceiros, setParceiros] = useState<ParceiroResumo[]>([])
  const [instrumentos, setInstrumentos] = useState<InstrumentoResumo[]>([])
  const [parcerias, setParcerias] = useState<Parceria[]>([])
  const [projetos, setProjetos] = useState<Projeto[]>([])
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [partnerId, setPartnerId] = useState('')
  const [instrumentId, setInstrumentId] = useState('')
  const [partnershipName, setPartnershipName] = useState('')
  const [projectPartnershipId, setProjectPartnershipId] = useState('')
  const [projectName, setProjectName] = useState('')
  const [projectDescription, setProjectDescription] = useState('')
  const [planProject, setPlanProject] = useState<Projeto | null>(null)
  const [planExists, setPlanExists] = useState(false)
  const [plan, setPlan] = useState<PlanoDraft>(EMPTY_PLANO)

  const carregar = useCallback(async () => {
    try {
      const [partnerList, instrumentList, partnershipList, projectList] = await Promise.all([
        projetoService.listarParceiros(),
        projetoService.listarInstrumentos(),
        projetoService.listarParcerias(),
        projetoService.listar(),
      ])
      setParceiros(partnerList)
      setInstrumentos(instrumentList)
      setParcerias(partnershipList)
      setProjetos(projectList)
    } catch (error) {
      toast.error(parseApiError(error).message)
    } finally {
      setLoading(false)
    }
  }, [toast])

  useEffect(() => {
    void carregar()
  }, [carregar])

  const instrumentosDoParceiro = useMemo(
    () => instrumentos.filter((item) => item.parceiro.id === partnerId),
    [instrumentos, partnerId],
  )

  const criarParceria = async (event: FormEvent) => {
    event.preventDefault()
    if (!partnershipName.trim() || !partnerId || !instrumentId) {
      toast.error('Preencha nome, parceiro e instrumento para cadastrar a parceria.')
      return
    }
    setSaving(true)
    try {
      await projetoService.criarParceria({
        nome: partnershipName.trim(),
        parceiroId: partnerId,
        instrumentoId: instrumentId,
      })
      setPartnershipName('')
      setPartnerId('')
      setInstrumentId('')
      toast.success('Parceria cadastrada.')
      await carregar()
    } catch (error) {
      toast.error(parseApiError(error).message)
    } finally {
      setSaving(false)
    }
  }

  const criarProjeto = async (event: FormEvent) => {
    event.preventDefault()
    if (!projectPartnershipId || !projectName.trim() || !projectDescription.trim()) {
      toast.error('Preencha todos os campos do projeto.')
      return
    }
    setSaving(true)
    try {
      await projetoService.criar({
        parceriaId: projectPartnershipId,
        nome: projectName.trim(),
        descricao: projectDescription.trim(),
      })
      setProjectName('')
      setProjectDescription('')
      toast.success('Projeto cadastrado e vinculado à parceria.')
      await carregar()
    } catch (error) {
      toast.error(parseApiError(error).message)
    } finally {
      setSaving(false)
    }
  }

  const abrirPlano = async (projeto: Projeto) => {
    setPlanProject(projeto)
    setSaving(true)
    try {
      if (projeto.possuiPlanoTrabalho) {
        const saved = await projetoService.buscarPlano(projeto.id)
        setPlanExists(true)
        setPlan({
          objetivos: saved.objetivos,
          cronogramaInicio: saved.cronogramaInicio,
          cronogramaFim: saved.cronogramaFim,
          responsaveis: saved.responsaveis,
          entregas: saved.entregas,
          marcos: saved.marcos,
        })
      } else {
        const start = new Date()
        const end = new Date(start)
        end.setFullYear(end.getFullYear() + 1)
        setPlanExists(false)
        setPlan({
          ...EMPTY_PLANO,
          cronogramaInicio: localDate(start),
          cronogramaFim: localDate(end),
        })
      }
    } catch (error) {
      toast.error(parseApiError(error).message)
      setPlanProject(null)
    } finally {
      setSaving(false)
    }
  }

  const salvarPlano = async (event: FormEvent) => {
    event.preventDefault()
    if (!planProject) return
    const objectives = plan.objetivos.map((value) => value.trim()).filter(Boolean)
    if (objectives.length === 0 || !plan.cronogramaInicio || !plan.cronogramaFim || !plan.responsaveis.trim()) {
      toast.error('Informe ao menos um objetivo, o cronograma e os responsáveis.')
      return
    }
    if (plan.cronogramaFim < plan.cronogramaInicio) {
      toast.error('A data final do cronograma não pode ser anterior à data inicial.')
      return
    }
    const deliveries = plan.entregas.filter((item) => item.descricao.trim())
    if (deliveries.some((item) => !item.dataPrevista || !item.responsavel.trim())) {
      toast.error('Cada entrega precisa de data prevista e responsável.')
      return
    }
    const milestones = plan.marcos.filter((item) => item.descricao.trim())
    if (milestones.some((item) => !item.dataPrevista)) {
      toast.error('Cada marco precisa de data prevista.')
      return
    }
    const data: PlanoTrabalhoPayload = {
      objetivos: objectives,
      cronogramaInicio: plan.cronogramaInicio,
      cronogramaFim: plan.cronogramaFim,
      responsaveis: plan.responsaveis.trim(),
      entregas: deliveries.map((item) => ({
        descricao: item.descricao.trim(),
        dataPrevista: item.dataPrevista,
        responsavel: item.responsavel.trim(),
      })),
      marcos: milestones.map((item) => ({
        descricao: item.descricao.trim(),
        dataPrevista: item.dataPrevista,
      })),
    }
    setSaving(true)
    try {
      if (planExists) await projetoService.atualizarPlano(planProject.id, data)
      else await projetoService.criarPlano(planProject.id, data)
      toast.success('Plano de trabalho salvo.')
      setPlanProject(null)
      await carregar()
    } catch (error) {
      toast.error(parseApiError(error).message)
    } finally {
      setSaving(false)
    }
  }

  const excluirPlano = async () => {
    if (!planProject || !planExists) return
    setSaving(true)
    try {
      await projetoService.excluirPlano(planProject.id)
      toast.success('Plano de trabalho excluído.')
      setPlanProject(null)
      await carregar()
    } catch (error) {
      toast.error(parseApiError(error).message)
    } finally {
      setSaving(false)
    }
  }

  const excluirProjeto = async (projeto: Projeto) => {
    if (!window.confirm(`Cancelar o projeto "${projeto.nome}"?`)) return
    setSaving(true)
    try {
      await projetoService.excluir(projeto.id)
      toast.success('Projeto cancelado.')
      await carregar()
    } catch (error) {
      toast.error(parseApiError(error).message)
    } finally {
      setSaving(false)
    }
  }

  const labelClass = 'flex flex-col gap-1 text-sm font-medium text-zinc-700'

  return (
    <div className="flex flex-col gap-6">
      <header>
        <h1 className="text-2xl font-bold">Parcerias e Projetos</h1>
        <p className="mt-1 text-sm text-zinc-600">Organize projetos e planos de trabalho vinculados a parcerias formalizadas.</p>
      </header>

      <form onSubmit={criarParceria} className="grid grid-cols-1 gap-4 rounded-xl border border-zinc-200 bg-white p-5 md:grid-cols-3">
        <h2 className="text-lg font-semibold md:col-span-3">Cadastrar parceria formalizada</h2>
        <label className={labelClass}>
          Nome da parceria
          <InputText value={partnershipName} maxLength={200} onChange={(event) => setPartnershipName(event.target.value)} />
        </label>
        <label className={labelClass}>
          Parceiro
          <Dropdown
            value={partnerId || null}
            options={parceiros.filter((item) => item.status !== 'INATIVO')}
            optionLabel="razaoSocial"
            optionValue="id"
            placeholder="Selecione um parceiro"
            onChange={(event) => {
              setPartnerId(event.value ?? '')
              setInstrumentId('')
            }}
          />
        </label>
        <label className={labelClass}>
          Instrumento jurídico
          <Dropdown
            value={instrumentId || null}
            options={instrumentosDoParceiro}
            optionLabel="nome"
            optionValue="id"
            placeholder={partnerId ? 'Selecione um instrumento' : 'Selecione primeiro o parceiro'}
            disabled={!partnerId}
            onChange={(event) => setInstrumentId(event.value ?? '')}
          />
        </label>
        <div className="flex justify-end md:col-span-3">
          <Button type="submit" label="Cadastrar parceria" icon="pi pi-link" loading={saving} disabled={!partnershipName.trim() || !partnerId || !instrumentId} />
        </div>
      </form>

      <section className="rounded-xl border border-zinc-200 bg-white p-5">
        <h2 className="mb-3 text-lg font-semibold">Parcerias cadastradas</h2>
        {parcerias.length === 0 ? (
          <p className="text-sm text-zinc-500">Cadastre uma parceria a partir de um instrumento jurídico existente.</p>
        ) : (
          <ul className="flex flex-col divide-y divide-zinc-100">
            {parcerias.map((parceria) => (
              <li key={parceria.id} className="flex flex-wrap justify-between gap-2 py-3 text-sm">
                <span className="font-medium">{parceria.nome} · {parceria.parceiroNome}</span>
                <span className="text-zinc-500">{parceria.instrumentoNome} · {parceria.status}</span>
              </li>
            ))}
          </ul>
        )}
      </section>

      <form onSubmit={criarProjeto} className="grid grid-cols-1 gap-4 rounded-xl border border-zinc-200 bg-white p-5 md:grid-cols-2">
        <h2 className="text-lg font-semibold md:col-span-2">Cadastrar projeto</h2>
        <label className={labelClass}>
          Parceria
          <Dropdown
            value={projectPartnershipId || null}
            options={parcerias.filter((item) => item.status === 'ATIVA')}
            optionLabel="nome"
            optionValue="id"
            placeholder="Selecione uma parceria ativa"
            onChange={(event) => setProjectPartnershipId(event.value ?? '')}
          />
        </label>
        <label className={labelClass}>
          Nome do projeto
          <InputText value={projectName} maxLength={200} onChange={(event) => setProjectName(event.target.value)} />
        </label>
        <label className={`${labelClass} md:col-span-2`}>
          Descrição
          <InputTextarea rows={3} value={projectDescription} onChange={(event) => setProjectDescription(event.target.value)} />
        </label>
        <div className="flex justify-end md:col-span-2">
          <Button type="submit" label="Cadastrar projeto" icon="pi pi-plus" loading={saving} disabled={!projectPartnershipId || !projectName.trim() || !projectDescription.trim()} />
        </div>
      </form>

      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-semibold">Projetos</h2>
        {loading ? <p className="text-sm text-zinc-500">Carregando projetos...</p>
          : projetos.length === 0 ? <p className="text-sm text-zinc-500">Nenhum projeto cadastrado.</p>
            : projetos.map((projeto) => (
              <article key={projeto.id} className="rounded-xl border border-zinc-200 bg-white p-5">
                <div className="flex flex-wrap items-start justify-between gap-3">
                  <div>
                    <h3 className="font-semibold">{projeto.nome}</h3>
                    <p className="text-sm text-zinc-600">{projeto.parceriaNome} · {projeto.parceiroNome}</p>
                    <p className="mt-2 text-sm">{projeto.descricao}</p>
                  </div>
                  <span className="rounded-full bg-zinc-100 px-3 py-1 text-xs">{projeto.status}</span>
                </div>
                <div className="mt-4 flex gap-2">
                  <Button
                    type="button"
                    label={projeto.possuiPlanoTrabalho ? 'Ver/editar plano de trabalho' : 'Cadastrar plano de trabalho'}
                    icon={projeto.possuiPlanoTrabalho ? 'pi pi-pencil' : 'pi pi-list'}
                    outlined
                    onClick={() => void abrirPlano(projeto)}
                    disabled={saving}
                  />
                  {projeto.status !== 'CANCELADO' && (
                    <Button type="button" label="Cancelar projeto" icon="pi pi-times" severity="danger" text onClick={() => void excluirProjeto(projeto)} loading={saving} />
                  )}
                </div>
              </article>
            ))}
      </section>

      {planProject && (
        <form onSubmit={salvarPlano} className="flex flex-col gap-4 rounded-xl border-2 border-zinc-300 bg-white p-5">
          <div className="flex flex-wrap items-center justify-between gap-2">
            <h2 className="text-lg font-semibold">Plano de trabalho · {planProject.nome}</h2>
            <Button type="button" label="Fechar" text onClick={() => setPlanProject(null)} />
          </div>
          <section className="flex flex-col gap-2">
            <h3 className="font-medium">Objetivos</h3>
            {plan.objetivos.map((objective, index) => (
              <div key={`objective-${index}`} className="flex gap-2">
                <InputText
                  className="w-full"
                  value={objective}
                  onChange={(event) => setPlan((current) => ({
                    ...current,
                    objetivos: current.objetivos.map((value, row) => row === index ? event.target.value : value),
                  }))}
                />
                <Button type="button" icon="pi pi-trash" text severity="danger" onClick={() => setPlan((current) => ({
                  ...current,
                  objetivos: current.objetivos.filter((_, row) => row !== index),
                }))} />
              </div>
            ))}
            <Button type="button" label="Adicionar objetivo" icon="pi pi-plus" text onClick={() => setPlan((current) => ({ ...current, objetivos: [...current.objetivos, ''] }))} />
          </section>
          <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
            <label className={labelClass}>Início do cronograma<InputText type="date" value={plan.cronogramaInicio} onChange={(event) => setPlan({ ...plan, cronogramaInicio: event.target.value })} /></label>
            <label className={labelClass}>Fim do cronograma<InputText type="date" value={plan.cronogramaFim} min={plan.cronogramaInicio} onChange={(event) => setPlan({ ...plan, cronogramaFim: event.target.value })} /></label>
            <label className={`${labelClass} sm:col-span-2`}>Responsáveis<InputTextarea rows={2} value={plan.responsaveis} onChange={(event) => setPlan({ ...plan, responsaveis: event.target.value })} /></label>
          </div>

          <section className="flex flex-col gap-2">
            <h3 className="font-medium">Entregas</h3>
            {plan.entregas.map((delivery, index) => (
              <div key={`delivery-${index}`} className="grid grid-cols-1 items-end gap-2 sm:grid-cols-[1fr_180px_1fr_auto]">
                <label className={labelClass}>Descrição<InputText value={delivery.descricao} onChange={(event) => setPlan((current) => ({
                  ...current, entregas: current.entregas.map((item, row) => row === index ? { ...item, descricao: event.target.value } : item),
                }))} /></label>
                <label className={labelClass}>Data prevista<InputText type="date" value={delivery.dataPrevista} min={plan.cronogramaInicio} max={plan.cronogramaFim} onChange={(event) => setPlan((current) => ({
                  ...current, entregas: current.entregas.map((item, row) => row === index ? { ...item, dataPrevista: event.target.value } : item),
                }))} /></label>
                <label className={labelClass}>Responsável<InputText value={delivery.responsavel} onChange={(event) => setPlan((current) => ({
                  ...current, entregas: current.entregas.map((item, row) => row === index ? { ...item, responsavel: event.target.value } : item),
                }))} /></label>
                <Button type="button" icon="pi pi-trash" text severity="danger" onClick={() => setPlan((current) => ({
                  ...current, entregas: current.entregas.filter((_, row) => row !== index),
                }))} />
              </div>
            ))}
            <Button type="button" label="Adicionar entrega" icon="pi pi-plus" text onClick={() => setPlan((current) => ({
              ...current, entregas: [...current.entregas, { descricao: '', dataPrevista: '', responsavel: '' }],
            }))} />
          </section>

          <section className="flex flex-col gap-2">
            <h3 className="font-medium">Marcos</h3>
            {plan.marcos.map((milestone, index) => (
              <div key={`milestone-${index}`} className="grid grid-cols-1 items-end gap-2 sm:grid-cols-[1fr_180px_auto]">
                <label className={labelClass}>Descrição<InputText value={milestone.descricao} onChange={(event) => setPlan((current) => ({
                  ...current, marcos: current.marcos.map((item, row) => row === index ? { ...item, descricao: event.target.value } : item),
                }))} /></label>
                <label className={labelClass}>Data prevista<InputText type="date" value={milestone.dataPrevista} min={plan.cronogramaInicio} max={plan.cronogramaFim} onChange={(event) => setPlan((current) => ({
                  ...current, marcos: current.marcos.map((item, row) => row === index ? { ...item, dataPrevista: event.target.value } : item),
                }))} /></label>
                <Button type="button" icon="pi pi-trash" text severity="danger" onClick={() => setPlan((current) => ({
                  ...current, marcos: current.marcos.filter((_, row) => row !== index),
                }))} />
              </div>
            ))}
            <Button type="button" label="Adicionar marco" icon="pi pi-plus" text onClick={() => setPlan((current) => ({
              ...current, marcos: [...current.marcos, { descricao: '', dataPrevista: '' }],
            }))} />
          </section>

          <div className="flex justify-end gap-2">
            {planExists && <Button type="button" label="Excluir plano" severity="danger" outlined onClick={() => void excluirPlano()} loading={saving} />}
            <Button type="submit" label="Salvar plano" icon="pi pi-save" loading={saving} />
          </div>
        </form>
      )}
    </div>
  )
}
