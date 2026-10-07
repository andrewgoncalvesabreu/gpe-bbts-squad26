import { useEffect, useState, type FormEvent } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { Button } from 'primereact/button'
import { Dropdown } from 'primereact/dropdown'
import { InputText } from 'primereact/inputtext'
import { FormField } from '../../components/common/FormField'
import { useToast } from '../../components/common/toastContext'
import { PORTE_OPTIONS, STATUS_OPTIONS, TIPO_ORGANIZACAO_OPTIONS, UF_OPTIONS } from '../../constants/parceiro'
import { historicoParceiroService } from '../../services/historicoParceiroService'
import { parceiroService } from '../../services/parceiroService'
import type { Parceiro, ParceiroPayload } from '../../types/parceiro'
import { parseApiError } from '../../utils/apiError'
import { maskCep, maskCnpj, maskTelefone } from '../../utils/format'
import {
  emptyForm,
  formToPayload,
  parceiroToForm,
  type ParceiroFormValues,
} from '../../utils/parceiroMapper'
import { isValidCnpj, isValidEmail } from '../../utils/validators'
import { AreaChips } from './components/AreaChips'

type Errors = Partial<Record<keyof ParceiroFormValues, string>>

const CAMPOS_LABEL: Partial<Record<keyof ParceiroPayload, string>> = {
  razaoSocial: 'Razão Social',
  nomeFantasia: 'Nome Fantasia',
  cnpj: 'CNPJ',
  tipoOrganizacao: 'Tipo de Organização',
  segmentoAtuacao: 'Áreas de Atuação',
  porte: 'Porte',
  emailContato: 'E-mail',
  telefone: 'Telefone',
  enderecoCompleto: 'Endereço',
  responsaveis: 'Responsáveis',
  status: 'Status',
}

function validate(v: ParceiroFormValues): Errors {
  const e: Errors = {}
  if (!v.razaoSocial.trim()) e.razaoSocial = 'Razão Social é obrigatória'
  else if (v.razaoSocial.length > 200) e.razaoSocial = 'Máximo de 200 caracteres'
  if (v.nomeFantasia.length > 200) e.nomeFantasia = 'Máximo de 200 caracteres'
  if (!v.cnpj) e.cnpj = 'CNPJ é obrigatório'
  else if (!isValidCnpj(v.cnpj)) e.cnpj = 'CNPJ inválido'
  if (!v.tipoOrganizacao) e.tipoOrganizacao = 'Tipo de Organização é obrigatório'
  if (v.emailContato && !isValidEmail(v.emailContato)) e.emailContato = 'E-mail inválido'
  if (v.telefone.length > 20) e.telefone = 'Máximo de 20 caracteres'
  if (v.areas.join(', ').length > 100) e.areas = 'Selecione menos áreas (limite de 100 caracteres)'
  return e
}

export default function ParceiroFormPage() {
  const { id } = useParams<{ id: string }>()
  const isEdit = Boolean(id)
  const navigate = useNavigate()
  const toast = useToast()

  const [values, setValues] = useState<ParceiroFormValues>(emptyForm)
  const [original, setOriginal] = useState<Parceiro | null>(null)
  const [errors, setErrors] = useState<Errors>({})
  const [loading, setLoading] = useState(isEdit)
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    if (!id) return
    parceiroService
      .buscarPorId(id)
      .then((p) => {
        setOriginal(p)
        setValues(parceiroToForm(p))
      })
      .catch((err) => {
        toast.error(parseApiError(err).message)
        navigate('/parceiros', { replace: true })
      })
      .finally(() => setLoading(false))
  }, [id, navigate, toast])

  const set = <K extends keyof ParceiroFormValues>(key: K, value: ParceiroFormValues[K]) => {
    setValues((prev) => ({ ...prev, [key]: value }))
    setErrors((prev) => ({ ...prev, [key]: undefined }))
  }

  // RF003: grava no histórico (best-effort — falha aqui não desfaz o cadastro).
  // Se o back-end passar a registrar isso sozinho, remova esta função.
  const registrarHistorico = async (parceiroId: string, descricao: string) => {
    try {
      await historicoParceiroService.adicionar(parceiroId, { tipo: 'ALTERACAO_CADASTRAL', descricao })
    } catch {
      /* ignora */
    }
  }

  const onSubmit = async (ev: FormEvent) => {
    ev.preventDefault()
    const found = validate(values)
    setErrors(found)
    if (Object.keys(found).length > 0) {
      toast.error('Corrija os campos destacados.')
      return
    }

    const payload = formToPayload(values)
    setSaving(true)
    try {
      if (id && original) {
        await parceiroService.atualizar(id, payload)
        const alterados = (Object.keys(CAMPOS_LABEL) as (keyof ParceiroPayload)[])
          .filter((k) => (payload[k] ?? '') !== ((original[k] as string | null | undefined) ?? ''))
          .map((k) => CAMPOS_LABEL[k])
        if (alterados.length > 0) await registrarHistorico(id, `Campos alterados: ${alterados.join(', ')}.`)
        toast.success('Parceiro atualizado com sucesso.')
        navigate(`/parceiros/${id}`)
      } else {
        const criado = await parceiroService.criar(payload)
        await registrarHistorico(criado.id, 'Cadastro inicial do parceiro.')
        toast.success('Parceiro cadastrado com sucesso.')
        navigate(`/parceiros/${criado.id}`)
      }
    } catch (err) {
      const { message, fieldErrors, status } = parseApiError(err)
      if (status === 409) setErrors((prev) => ({ ...prev, cnpj: message }))
      if (Object.keys(fieldErrors).length > 0) setErrors((prev) => ({ ...prev, ...fieldErrors }))
      toast.error(message)
    } finally {
      setSaving(false)
    }
  }

  if (loading) return <p className="text-zinc-500">Carregando parceiro...</p>

  const section = 'rounded-xl border border-zinc-200 bg-white p-6'
  const grid = 'grid grid-cols-1 gap-4 md:grid-cols-6'

  return (
    <form onSubmit={onSubmit} noValidate className="mx-auto flex max-w-5xl flex-col gap-5">
      <h1 className="text-2xl font-bold">{isEdit ? 'Editar Parceiro' : 'Cadastrar Novo Parceiro'}</h1>

      <section className={section}>
        <h2 className="mb-4 text-lg font-semibold">Dados Institucionais</h2>
        <div className={grid}>
          <FormField label="Razão Social" required error={errors.razaoSocial} className="md:col-span-3">
            <InputText value={values.razaoSocial} onChange={(e) => set('razaoSocial', e.target.value)} placeholder="Insira a razão social completa" className={errors.razaoSocial ? 'p-invalid' : ''} />
          </FormField>
          <FormField label="Nome Fantasia" error={errors.nomeFantasia} className="md:col-span-3">
            <InputText value={values.nomeFantasia} onChange={(e) => set('nomeFantasia', e.target.value)} placeholder="Nome fantasia ou sigla" />
          </FormField>
          <FormField label="CNPJ" required error={errors.cnpj} className="md:col-span-2">
            <InputText value={maskCnpj(values.cnpj)} onChange={(e) => set('cnpj', e.target.value)} placeholder="00.000.000/0000-00" inputMode="text" className={errors.cnpj ? 'p-invalid' : ''} />
          </FormField>
          <FormField label="Tipo de Instituição" required error={errors.tipoOrganizacao} className="md:col-span-2">
            <Dropdown value={values.tipoOrganizacao || null} options={TIPO_ORGANIZACAO_OPTIONS} onChange={(e) => set('tipoOrganizacao', e.value ?? '')} placeholder="Selecione um tipo..." className={errors.tipoOrganizacao ? 'p-invalid' : ''} />
          </FormField>
          <FormField label="Porte" error={errors.porte} className="md:col-span-2">
            <Dropdown value={values.porte || null} options={PORTE_OPTIONS} onChange={(e) => set('porte', e.value ?? '')} placeholder="Selecione o porte..." showClear />
          </FormField>
          {isEdit && (
            <FormField label="Status" required error={errors.status} className="md:col-span-2">
              <Dropdown value={values.status} options={STATUS_OPTIONS} onChange={(e) => set('status', e.value)} />
            </FormField>
          )}
        </div>
      </section>

      <section className={section}>
        <h2 className="mb-4 text-lg font-semibold">Endereço</h2>
        <div className={grid}>
          <FormField label="CEP" className="md:col-span-2">
            <InputText value={values.cep} onChange={(e) => set('cep', maskCep(e.target.value))} placeholder="00000-000" inputMode="numeric" />
          </FormField>
          <FormField label="Logradouro" className="md:col-span-3">
            <InputText value={values.logradouro} onChange={(e) => set('logradouro', e.target.value)} placeholder="Avenida, Rua, etc." />
          </FormField>
          <FormField label="Número" className="md:col-span-1">
            <InputText value={values.numero} onChange={(e) => set('numero', e.target.value)} placeholder="123" />
          </FormField>
          <FormField label="Complemento" className="md:col-span-2">
            <InputText value={values.complemento} onChange={(e) => set('complemento', e.target.value)} placeholder="Sala, Bloco, etc." />
          </FormField>
          <FormField label="Cidade" className="md:col-span-2">
            <InputText value={values.cidade} onChange={(e) => set('cidade', e.target.value)} placeholder="Nome da cidade" />
          </FormField>
          <FormField label="UF" className="md:col-span-1">
            <Dropdown value={values.uf || null} options={UF_OPTIONS} onChange={(e) => set('uf', e.value ?? '')} placeholder="UF" filter />
          </FormField>
          <FormField label="País" className="md:col-span-1">
            <InputText value={values.pais} onChange={(e) => set('pais', e.target.value)} />
          </FormField>
        </div>
      </section>

      <section className={section}>
        <h2 className="mb-4 text-lg font-semibold">Contato Principal</h2>
        <div className={grid}>
          <FormField label="Nome do Responsável" className="md:col-span-3">
            <InputText value={values.responsavelNome} onChange={(e) => set('responsavelNome', e.target.value)} placeholder="Nome completo" />
          </FormField>
          <FormField label="Cargo" className="md:col-span-3">
            <InputText value={values.responsavelCargo} onChange={(e) => set('responsavelCargo', e.target.value)} placeholder="ex: Diretor de P&D" />
          </FormField>
          <FormField label="E-mail" error={errors.emailContato} className="md:col-span-3">
            <InputText type="email" value={values.emailContato} onChange={(e) => set('emailContato', e.target.value)} placeholder="contato@instituicao.org" className={errors.emailContato ? 'p-invalid' : ''} />
          </FormField>
          <FormField label="Telefone" error={errors.telefone} className="md:col-span-3">
            <InputText value={values.telefone} onChange={(e) => set('telefone', maskTelefone(e.target.value))} placeholder="(00) 00000-0000" inputMode="tel" />
          </FormField>
        </div>
      </section>

      <section className={section}>
        <h2 className="mb-4 text-lg font-semibold">Áreas de Atuação</h2>
        <AreaChips value={values.areas} onChange={(a) => set('areas', a)} />
        {errors.areas && <small className="mt-2 block text-red-600">{errors.areas}</small>}
      </section>

      <div className="flex justify-end gap-3">
        <Button type="button" label="Cancelar" outlined onClick={() => navigate(isEdit ? `/parceiros/${id}` : '/parceiros')} />
        <Button type="submit" label={isEdit ? 'Salvar Alterações' : 'Cadastrar Parceiro'} loading={saving} />
      </div>
    </form>
  )
}
