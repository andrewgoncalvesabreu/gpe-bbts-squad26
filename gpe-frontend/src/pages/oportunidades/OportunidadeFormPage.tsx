import { useEffect, useState, type FormEvent } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { Button } from 'primereact/button'
import { Dropdown } from 'primereact/dropdown'
import { InputNumber } from 'primereact/inputnumber'
import { InputText } from 'primereact/inputtext'
import { InputTextarea } from 'primereact/inputtextarea'
import { FormField } from '../../components/common/FormField'
import { useToast } from '../../components/common/toastContext'
import { ORIGEM_OPORTUNIDADE_OPTIONS } from '../../constants/oportunidade'
import { parceiroService } from '../../services/parceiroService'
import { oportunidadeService } from '../../services/oportunidadeService'
import type { Parceiro } from '../../types/parceiro'
import type { Oportunidade, OportunidadePayload } from '../../types/oportunidade'
import { parseApiError } from '../../utils/apiError'

type FormValues = Omit<OportunidadePayload, 'parceiro'> & { parceiroId: string }
type FormErrors = Partial<Record<keyof FormValues, string>>

const emptyForm: FormValues = {
  titulo: '', parceiroId: '', origem: '', descricao: '', objetivos: '', justificativa: '', beneficiosEsperados: '',
  problemaDemandado: '', solucaoProposta: '', estimativaFinanceira: null, areaDemandante: '', responsavelMatricula: '',
}

export default function OportunidadeFormPage() {
  const { id } = useParams<{ id: string }>()
  const isEdit = Boolean(id)
  const navigate = useNavigate()
  const toast = useToast()
  const [values, setValues] = useState<FormValues>(emptyForm)
  const [parceiros, setParceiros] = useState<Parceiro[]>([])
  const [errors, setErrors] = useState<FormErrors>({})
  const [loading, setLoading] = useState(isEdit)
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    Promise.all([
      parceiroService.listar(),
      id ? oportunidadeService.buscarPorId(id) : Promise.resolve<Oportunidade | null>(null),
    ]).then(([lista, oportunidade]) => {
      setParceiros(lista)
      if (oportunidade) setValues({
        titulo: oportunidade.titulo, parceiroId: oportunidade.parceiro.id, origem: oportunidade.origem,
        descricao: oportunidade.descricao ?? '', objetivos: oportunidade.objetivos ?? '', justificativa: oportunidade.justificativa ?? '',
        beneficiosEsperados: oportunidade.beneficiosEsperados ?? '', problemaDemandado: oportunidade.problemaDemandado ?? '',
        solucaoProposta: oportunidade.solucaoProposta ?? '', estimativaFinanceira: oportunidade.estimativaFinanceira ?? null,
        areaDemandante: oportunidade.areaDemandante, responsavelMatricula: oportunidade.responsavelMatricula,
      })
    }).catch((err) => {
      toast.error(parseApiError(err).message)
      navigate('/oportunidades', { replace: true })
    }).finally(() => setLoading(false))
  }, [id, navigate, toast])

  const set = <K extends keyof FormValues>(key: K, value: FormValues[K]) => {
    setValues((previous) => ({ ...previous, [key]: value }))
    setErrors((previous) => ({ ...previous, [key]: undefined }))
  }

  const validate = () => {
    const next: FormErrors = {}
    const required: [keyof FormValues, string][] = [
      ['titulo', 'Título é obrigatório'], ['parceiroId', 'Selecione um parceiro'], ['origem', 'Origem é obrigatória'],
      ['descricao', 'Descrição é obrigatória'], ['objetivos', 'Objetivos são obrigatórios'], ['justificativa', 'Justificativa é obrigatória'],
      ['beneficiosEsperados', 'Benefícios esperados são obrigatórios'], ['areaDemandante', 'Área demandante é obrigatória'],
      ['responsavelMatricula', 'Responsável é obrigatório'],
    ]
    required.forEach(([key, message]) => { if (!String(values[key] ?? '').trim()) next[key] = message })
    if (values.titulo.length > 200) next.titulo = 'Máximo de 200 caracteres'
    if (values.areaDemandante.length > 100) next.areaDemandante = 'Máximo de 100 caracteres'
    if (values.responsavelMatricula.length > 50) next.responsavelMatricula = 'Máximo de 50 caracteres'
    setErrors(next)
    return Object.keys(next).length === 0
  }

  const onSubmit = async (event: FormEvent) => {
    event.preventDefault()
    if (!validate()) { toast.error('Corrija os campos destacados.'); return }
    setSaving(true)
    const payload: OportunidadePayload = {
      titulo: values.titulo.trim(), parceiro: { id: values.parceiroId }, origem: values.origem,
      descricao: values.descricao.trim(), objetivos: values.objetivos.trim(), justificativa: values.justificativa.trim(),
      beneficiosEsperados: values.beneficiosEsperados.trim(), problemaDemandado: values.problemaDemandado?.trim() || null,
      solucaoProposta: values.solucaoProposta?.trim() || null, estimativaFinanceira: values.estimativaFinanceira,
      areaDemandante: values.areaDemandante.trim(), responsavelMatricula: values.responsavelMatricula.trim(),
    }
    try {
      const salvo = id ? await oportunidadeService.atualizar(id, payload) : await oportunidadeService.criar(payload)
      toast.success(isEdit ? 'Oportunidade atualizada com sucesso.' : 'Oportunidade cadastrada com sucesso.')
      navigate(`/oportunidades/${salvo.id}`)
    } catch (err) { toast.error(parseApiError(err).message) } finally { setSaving(false) }
  }

  if (loading) return <p className="text-zinc-500">Carregando oportunidade...</p>
  const section = 'rounded-xl border border-zinc-200 bg-white p-6'
  const grid = 'grid grid-cols-1 gap-4 md:grid-cols-2'
  const parceiroOptions = parceiros.map((item) => ({ label: item.nomeFantasia ? `${item.nomeFantasia} — ${item.razaoSocial}` : item.razaoSocial, value: item.id }))

  return (
    <form onSubmit={onSubmit} noValidate className="mx-auto flex max-w-5xl flex-col gap-5">
      <div><h1 className="text-2xl font-bold">{isEdit ? 'Editar Oportunidade' : 'Nova Oportunidade'}</h1><p className="mt-1 text-sm text-zinc-500">Registre uma oportunidade e envie-a para o fluxo de avaliação.</p></div>
      <section className={section}><h2 className="mb-4 text-lg font-semibold">Identificação</h2><div className={grid}>
        <FormField label="Título" required error={errors.titulo} className="md:col-span-2"><InputText value={values.titulo} onChange={(e) => set('titulo', e.target.value)} placeholder="Ex.: Parceria para modernização de serviços" className={errors.titulo ? 'p-invalid' : ''} /></FormField>
        <FormField label="Parceiro" required error={errors.parceiroId}><Dropdown value={values.parceiroId || null} options={parceiroOptions} onChange={(e) => set('parceiroId', e.value ?? '')} placeholder="Selecione um parceiro" filter className={errors.parceiroId ? 'p-invalid' : ''} /></FormField>
        <FormField label="Origem" required error={errors.origem}><Dropdown value={values.origem || null} options={ORIGEM_OPORTUNIDADE_OPTIONS} onChange={(e) => set('origem', e.value ?? '')} placeholder="Selecione a origem" className={errors.origem ? 'p-invalid' : ''} /></FormField>
        <FormField label="Área demandante" required error={errors.areaDemandante}><InputText value={values.areaDemandante} onChange={(e) => set('areaDemandante', e.target.value)} placeholder="Área responsável pela demanda" className={errors.areaDemandante ? 'p-invalid' : ''} /></FormField>
        <FormField label="Responsável (matrícula)" required error={errors.responsavelMatricula}><InputText value={values.responsavelMatricula} onChange={(e) => set('responsavelMatricula', e.target.value)} placeholder="Matrícula do responsável" className={errors.responsavelMatricula ? 'p-invalid' : ''} /></FormField>
        <FormField label="Estimativa financeira"><InputNumber value={values.estimativaFinanceira} onValueChange={(e) => set('estimativaFinanceira', e.value ?? null)} mode="currency" currency="BRL" locale="pt-BR" min={0} className="w-full" inputClassName="w-full" /></FormField>
      </div></section>
      <section className={section}><h2 className="mb-4 text-lg font-semibold">Detalhamento</h2><div className="flex flex-col gap-4">
        <FormField label="Descrição" required error={errors.descricao}><InputTextarea value={values.descricao} onChange={(e) => set('descricao', e.target.value)} rows={4} placeholder="Descreva a oportunidade" className={errors.descricao ? 'p-invalid' : ''} /></FormField>
        <div className={grid}><FormField label="Objetivos" required error={errors.objetivos}><InputTextarea value={values.objetivos} onChange={(e) => set('objetivos', e.target.value)} rows={4} className={errors.objetivos ? 'p-invalid' : ''} /></FormField><FormField label="Justificativa" required error={errors.justificativa}><InputTextarea value={values.justificativa} onChange={(e) => set('justificativa', e.target.value)} rows={4} className={errors.justificativa ? 'p-invalid' : ''} /></FormField><FormField label="Benefícios esperados" required error={errors.beneficiosEsperados}><InputTextarea value={values.beneficiosEsperados} onChange={(e) => set('beneficiosEsperados', e.target.value)} rows={4} className={errors.beneficiosEsperados ? 'p-invalid' : ''} /></FormField><FormField label="Problema demandado"><InputTextarea value={values.problemaDemandado ?? ''} onChange={(e) => set('problemaDemandado', e.target.value)} rows={4} /></FormField><FormField label="Solução proposta"><InputTextarea value={values.solucaoProposta ?? ''} onChange={(e) => set('solucaoProposta', e.target.value)} rows={4} /></FormField></div>
      </div></section>
      <div className="flex justify-end gap-3"><Button type="button" label="Cancelar" outlined onClick={() => navigate(isEdit ? `/oportunidades/${id}` : '/oportunidades')} /><Button type="submit" label={isEdit ? 'Salvar Alterações' : 'Cadastrar Oportunidade'} loading={saving} /></div>
    </form>
  )
}
