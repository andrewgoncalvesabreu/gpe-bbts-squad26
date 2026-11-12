import { useCallback, useEffect, useState, type FormEvent } from 'react'
import { Button } from 'primereact/button'
import { useToast } from '../../components/common/toastContext'
import { oportunidadeService } from '../../services/oportunidadeService'
import type { AvaliacaoPayload, Oportunidade, WorkflowPayload } from '../../types/oportunidade'
import type { Parceiro } from '../../types/parceiro'
import { parseApiError } from '../../utils/apiError'

interface FormValues {
  titulo: string
  parceiroId: string
  origem: string
  descricao: string
  objetivos: string
  justificativa: string
  beneficiosEsperados: string
  areaDemandante: string
  responsavelMatricula: string
}

interface EvaluationValues {
  alinhamentoEstrategico: number
  potencialInovacao: number
  retornoFinanceiro: number
  impactoInstitucional: number
  viabilidadeTecnica: number
  parecerTecnico: string
}

const EMPTY_FORM: FormValues = {
  titulo: '',
  parceiroId: '',
  origem: '',
  descricao: '',
  objetivos: '',
  justificativa: '',
  beneficiosEsperados: '',
  areaDemandante: '',
  responsavelMatricula: '',
}

const EMPTY_EVALUATION: EvaluationValues = {
  alinhamentoEstrategico: 3,
  potencialInovacao: 3,
  retornoFinanceiro: 3,
  impactoInstitucional: 3,
  viabilidadeTecnica: 3,
  parecerTecnico: '',
}

const CRITERIA: { key: keyof EvaluationValues; label: string }[] = [
  { key: 'alinhamentoEstrategico', label: 'Alinhamento estratégico' },
  { key: 'potencialInovacao', label: 'Potencial de inovação' },
  { key: 'retornoFinanceiro', label: 'Retorno financeiro' },
  { key: 'impactoInstitucional', label: 'Impacto institucional' },
  { key: 'viabilidadeTecnica', label: 'Viabilidade técnica' },
]

const STATUS_LABEL: Record<Oportunidade['statusWorkflow'], string> = {
  TRIAGEM: 'Triagem',
  EM_AVALIACAO: 'Em avaliação',
  EM_APROVACAO: 'Em aprovação',
  APROVADA: 'Aprovada',
  REJEITADA: 'Rejeitada',
  CANCELADA: 'Cancelada',
}

export default function OportunidadesPage() {
  const toast = useToast()
  const [oportunidades, setOportunidades] = useState<Oportunidade[] | null>(null)
  const [parceiros, setParceiros] = useState<Parceiro[]>([])
  const [saving, setSaving] = useState(false)
  const [values, setValues] = useState(EMPTY_FORM)
  const [errors, setErrors] = useState<Partial<Record<keyof FormValues, string>>>({})
  const [operadorMatricula, setOperadorMatricula] = useState('')
  const [avaliacaoId, setAvaliacaoId] = useState<string | null>(null)
  const [avaliacao, setAvaliacao] = useState(EMPTY_EVALUATION)
  const [justificativas, setJustificativas] = useState<Record<string, string>>({})

  const carregar = useCallback(async () => {
    try {
      const [lista, parceirosDisponiveis] = await Promise.all([
        oportunidadeService.listar(),
        oportunidadeService.listarParceiros(),
      ])
      setOportunidades(lista)
      setParceiros(parceirosDisponiveis)
    } catch (error) {
      toast.error(parseApiError(error).message)
      setOportunidades([])
    }
  }, [toast])

  useEffect(() => {
    void carregar()
  }, [carregar])

  const submit = async (event: FormEvent) => {
    event.preventDefault()
    const nextErrors: Partial<Record<keyof FormValues, string>> = {}
    for (const key of ['titulo', 'parceiroId', 'origem', 'descricao', 'objetivos', 'justificativa', 'beneficiosEsperados', 'areaDemandante', 'responsavelMatricula'] as const) {
      if (!values[key].trim()) nextErrors[key] = 'Campo obrigatório'
    }
    setErrors(nextErrors)
    if (Object.keys(nextErrors).length || !operadorMatricula.trim()) {
      if (!operadorMatricula.trim()) toast.error('Informe a matrícula de quem está cadastrando a oportunidade.')
      return
    }

    setSaving(true)
    try {
      const { parceiroId, ...campos } = values
      await oportunidadeService.criar({
        ...campos,
        titulo: values.titulo.trim(),
        parceiro: { id: parceiroId },
        origem: values.origem.trim(),
        descricao: values.descricao.trim(),
        objetivos: values.objetivos.trim(),
        justificativa: values.justificativa.trim(),
        beneficiosEsperados: values.beneficiosEsperados.trim(),
        areaDemandante: values.areaDemandante.trim(),
        responsavelMatricula: values.responsavelMatricula.trim(),
      })
      setValues(EMPTY_FORM)
      toast.success('Oportunidade cadastrada.')
      await carregar()
    } catch (error) {
      toast.error(parseApiError(error).message)
    } finally {
      setSaving(false)
    }
  }

  const salvarAvaliacao = async (event: FormEvent) => {
    event.preventDefault()
    if (!avaliacaoId) return
    if (!operadorMatricula.trim()) {
      toast.error('Informe a matrícula de quem está avaliando.')
      return
    }
    const dados: AvaliacaoPayload = {
      ...avaliacao,
      parecerTecnico: avaliacao.parecerTecnico.trim(),
      avaliadorMatricula: operadorMatricula.trim(),
    }
    setSaving(true)
    try {
      await oportunidadeService.avaliar(avaliacaoId, dados)
      setAvaliacaoId(null)
      setAvaliacao(EMPTY_EVALUATION)
      toast.success('Avaliação registrada.')
      await carregar()
    } catch (error) {
      toast.error(parseApiError(error).message)
    } finally {
      setSaving(false)
    }
  }

  const workflow = async (
    oportunidade: Oportunidade,
    action: (id: string, payload: WorkflowPayload) => Promise<Oportunidade>,
    justificativa?: string,
  ) => {
    if (!operadorMatricula.trim()) {
      toast.error('Informe a matrícula do responsável pela operação.')
      return
    }
    if (action === oportunidadeService.reprovar && !justificativa?.trim()) {
      toast.error('A justificativa da reprovação é obrigatória.')
      return
    }
    setSaving(true)
    try {
      await action(oportunidade.id, {
        responsavelMatricula: operadorMatricula.trim(),
        justificativa: justificativa?.trim(),
      })
      toast.success('Fluxo da oportunidade atualizado.')
      setJustificativas((current) => ({ ...current, [oportunidade.id]: '' }))
      await carregar()
    } catch (error) {
      toast.error(parseApiError(error).message)
    } finally {
      setSaving(false)
    }
  }

  const fieldClass = 'w-full rounded-md border border-zinc-300 px-3 py-2 text-sm'
  const labelClass = 'flex flex-col gap-1 text-sm font-medium text-zinc-700'

  return (
    <div className="flex flex-col gap-6">
      <header>
        <h1 className="text-2xl font-bold">Oportunidades de Parceria</h1>
        <p className="mt-1 text-sm text-zinc-600">Registre, avalie e acompanhe o fluxo de aprovação.</p>
      </header>

      <label className={labelClass}>
        Matrícula de quem está operando
        <input
          className={fieldClass}
          maxLength={50}
          value={operadorMatricula}
          onChange={(event) => setOperadorMatricula(event.target.value)}
          placeholder="Necessária para registrar avaliações e decisões"
        />
      </label>

      <form onSubmit={submit} className="grid grid-cols-1 gap-4 rounded-xl border border-zinc-200 bg-white p-5 md:grid-cols-2">
        <h2 className="text-lg font-semibold md:col-span-2">Registrar oportunidade</h2>
        <label className={labelClass}>
          Título
          <input className={fieldClass} value={values.titulo} onChange={(e) => setValues({ ...values, titulo: e.target.value })} />
          {errors.titulo && <small className="text-red-600">{errors.titulo}</small>}
        </label>
        <label className={labelClass}>
          Parceiro
          <select className={fieldClass} value={values.parceiroId} onChange={(e) => setValues({ ...values, parceiroId: e.target.value })}>
            <option value="">Selecione</option>
            {parceiros.filter((item) => item.status !== 'INATIVO').map((item) => (
              <option key={item.id} value={item.id}>{item.razaoSocial}</option>
            ))}
          </select>
          {errors.parceiroId && <small className="text-red-600">{errors.parceiroId}</small>}
        </label>
        <label className={labelClass}>
          Origem
          <input className={fieldClass} value={values.origem} onChange={(e) => setValues({ ...values, origem: e.target.value })} />
          {errors.origem && <small className="text-red-600">{errors.origem}</small>}
        </label>
        <label className={labelClass}>
          Área demandante
          <input className={fieldClass} value={values.areaDemandante} onChange={(e) => setValues({ ...values, areaDemandante: e.target.value })} />
          {errors.areaDemandante && <small className="text-red-600">{errors.areaDemandante}</small>}
        </label>
        <label className={labelClass}>
          Matrícula do responsável pela oportunidade
          <input className={fieldClass} maxLength={50} value={values.responsavelMatricula} onChange={(e) => setValues({ ...values, responsavelMatricula: e.target.value })} />
          {errors.responsavelMatricula && <small className="text-red-600">{errors.responsavelMatricula}</small>}
        </label>
        {([
          ['descricao', 'Descrição'],
          ['objetivos', 'Objetivos'],
          ['justificativa', 'Justificativa'],
          ['beneficiosEsperados', 'Benefícios esperados'],
        ] as const).map(([key, label]) => (
          <label key={key} className={`${labelClass} md:col-span-2`}>
            {label}
            <textarea className={fieldClass} rows={3} value={values[key]} onChange={(e) => setValues({ ...values, [key]: e.target.value })} />
            {errors[key] && <small className="text-red-600">{errors[key]}</small>}
          </label>
        ))}
        <div className="flex justify-end md:col-span-2">
          <Button type="submit" label="Cadastrar oportunidade" loading={saving} />
        </div>
      </form>

      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-semibold">Oportunidades cadastradas</h2>
        {oportunidades === null ? <p className="text-sm text-zinc-500">Carregando oportunidades...</p>
          : oportunidades.length === 0 ? <p className="text-sm text-zinc-500">Nenhuma oportunidade cadastrada.</p>
            : oportunidades.map((item) => (
              <article key={item.id} className="rounded-xl border border-zinc-200 bg-white p-5">
                <div className="flex flex-wrap items-start justify-between gap-3">
                  <div>
                    <h3 className="font-semibold">{item.titulo}</h3>
                    <p className="text-sm text-zinc-600">{item.parceiro.razaoSocial} · {item.areaDemandante}</p>
                  </div>
                  <span className="rounded-full bg-zinc-100 px-3 py-1 text-xs font-medium">{STATUS_LABEL[item.statusWorkflow]}</span>
                </div>
                <p className="mt-3 text-sm text-zinc-700">{item.descricao}</p>
                <div className="mt-4 flex flex-wrap gap-2">
                  {(item.statusWorkflow === 'TRIAGEM' || item.statusWorkflow === 'EM_AVALIACAO') && (
                    <Button type="button" label="Avaliar" outlined onClick={() => setAvaliacaoId(item.id)} />
                  )}
                  {item.statusWorkflow === 'EM_AVALIACAO' && (
                    <Button
                      type="button"
                      label="Enviar para aprovação"
                      onClick={() => void workflow(item, oportunidadeService.enviarParaAprovacao)}
                      loading={saving}
                    />
                  )}
                  {item.statusWorkflow === 'EM_APROVACAO' && (
                    <>
                      <Button type="button" label="Aprovar" onClick={() => void workflow(item, oportunidadeService.aprovar)} loading={saving} />
                      <input
                        className={`${fieldClass} max-w-sm`}
                        placeholder="Justificativa obrigatória para reprovar"
                        value={justificativas[item.id] ?? ''}
                        onChange={(event) => setJustificativas((current) => ({ ...current, [item.id]: event.target.value }))}
                      />
                      <Button
                        type="button"
                        label="Reprovar"
                        severity="danger"
                        outlined
                        onClick={() => void workflow(item, oportunidadeService.reprovar, justificativas[item.id])}
                        loading={saving}
                      />
                    </>
                  )}
                </div>

                {avaliacaoId === item.id && (
                  <form onSubmit={salvarAvaliacao} className="mt-4 grid grid-cols-1 gap-3 border-t border-zinc-200 pt-4 sm:grid-cols-2">
                    <h4 className="font-semibold sm:col-span-2">Avaliação (notas de 1 a 5)</h4>
                    {CRITERIA.map(({ key, label }) => (
                      <label key={key} className={labelClass}>
                        {label}
                        <select
                          className={fieldClass}
                          value={avaliacao[key]}
                          onChange={(event) => setAvaliacao({ ...avaliacao, [key]: Number(event.target.value) })}
                        >
                          {[1, 2, 3, 4, 5].map((score) => <option key={score} value={score}>{score}</option>)}
                        </select>
                      </label>
                    ))}
                    <label className={`${labelClass} sm:col-span-2`}>
                      Parecer técnico
                      <textarea className={fieldClass} rows={3} value={avaliacao.parecerTecnico} onChange={(event) => setAvaliacao({ ...avaliacao, parecerTecnico: event.target.value })} />
                    </label>
                    <div className="flex gap-2 sm:col-span-2">
                      <Button type="submit" label="Salvar avaliação" loading={saving} />
                      <Button type="button" label="Cancelar" outlined onClick={() => setAvaliacaoId(null)} />
                    </div>
                  </form>
                )}
              </article>
            ))}
      </section>
    </div>
  )
}
