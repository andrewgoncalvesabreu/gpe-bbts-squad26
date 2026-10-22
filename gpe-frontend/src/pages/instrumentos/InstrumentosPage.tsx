import { useEffect, useMemo, useState, type FormEvent } from 'react'
import { Button } from 'primereact/button'
import { Column } from 'primereact/column'
import { DataTable } from 'primereact/datatable'
import { Dialog } from 'primereact/dialog'
import { Dropdown } from 'primereact/dropdown'
import { InputText } from 'primereact/inputtext'
import { InputTextarea } from 'primereact/inputtextarea'
import { FormField } from '../../components/common/FormField'
import { useToast } from '../../components/common/toastContext'
import { TIPO_INSTRUMENTO_OPTIONS } from '../../constants/instrumento'
import { instrumentoService } from '../../services/instrumentoService'
import type {
  InstrumentoJuridico,
  OportunidadeResumo,
  ProrrogacaoInstrumento,
  TipoInstrumento,
} from '../../types/instrumento'
import type { Parceiro } from '../../types/parceiro'
import { parseApiError } from '../../utils/apiError'
import { formatDate } from '../../utils/format'
import { AlertasVencimento } from './components/AlertasVencimento'

interface FormValues {
  nome: string
  tipo: TipoInstrumento | ''
  numeroInstrumento: string
  descricao: string
  dataInicioVigencia: string
  dataFimVigencia: string
  parceiroId: string
  oportunidadeId: string
}

const EMPTY_FORM: FormValues = {
  nome: '',
  tipo: '',
  numeroInstrumento: '',
  descricao: '',
  dataInicioVigencia: '',
  dataFimVigencia: '',
  parceiroId: '',
  oportunidadeId: '',
}

async function carregarDados() {
  const [instrumentos, parceiros, oportunidades] = await Promise.all([
    instrumentoService.listar(),
    instrumentoService.listarParceiros(),
    instrumentoService.listarOportunidades(),
  ])
  return { instrumentos, parceiros, oportunidades }
}

export default function InstrumentosPage() {
  const toast = useToast()
  const [instrumentos, setInstrumentos] = useState<InstrumentoJuridico[]>([])
  const [parceiros, setParceiros] = useState<Parceiro[]>([])
  const [oportunidades, setOportunidades] = useState<OportunidadeResumo[]>([])
  const [loading, setLoading] = useState(true)
  const [alertasRefreshToken, setAlertasRefreshToken] = useState(0)
  const [dialogOpen, setDialogOpen] = useState(false)
  const [prorrogacaoOpen, setProrrogacaoOpen] = useState(false)
  const [saving, setSaving] = useState(false)
  const [loadingProrrogacoes, setLoadingProrrogacoes] = useState(false)
  const [savingProrrogacao, setSavingProrrogacao] = useState(false)
  const [values, setValues] = useState<FormValues>(EMPTY_FORM)
  const [errors, setErrors] = useState<Partial<Record<keyof FormValues, string>>>({})
  const [instrumentoSelecionado, setInstrumentoSelecionado] = useState<InstrumentoJuridico | null>(null)
  const [prorrogacoes, setProrrogacoes] = useState<ProrrogacaoInstrumento[]>([])
  const [novaDataFim, setNovaDataFim] = useState('')
  const [justificativa, setJustificativa] = useState('')
  const [erroProrrogacao, setErroProrrogacao] = useState('')

  useEffect(() => {
    let ativo = true
    carregarDados()
      .then((dados) => {
        if (!ativo) return
        setInstrumentos(dados.instrumentos)
        setParceiros(dados.parceiros)
        setOportunidades(dados.oportunidades)
      })
      .catch((error) => {
        if (ativo) toast.error(parseApiError(error).message)
      })
      .finally(() => {
        if (ativo) setLoading(false)
      })
    return () => {
      ativo = false
    }
  }, [toast])

  const oportunidadesDoParceiro = useMemo(
    () => oportunidades.filter((item) => item.parceiro.id === values.parceiroId),
    [oportunidades, values.parceiroId],
  )

  const setValue = <K extends keyof FormValues>(key: K, value: FormValues[K]) => {
    setValues((previous) => ({ ...previous, [key]: value }))
    setErrors((previous) => ({ ...previous, [key]: undefined }))
  }

  const fecharDialog = () => {
    setDialogOpen(false)
    setValues(EMPTY_FORM)
    setErrors({})
  }

  const salvar = async (event: FormEvent) => {
    event.preventDefault()
    const nextErrors: Partial<Record<keyof FormValues, string>> = {}
    if (!values.nome.trim()) nextErrors.nome = 'Nome do instrumento é obrigatório'
    if (!values.tipo) nextErrors.tipo = 'Selecione o tipo de instrumento'
    if (!values.parceiroId) nextErrors.parceiroId = 'Selecione o parceiro'
    if (!values.dataInicioVigencia) nextErrors.dataInicioVigencia = 'Data de início é obrigatória'
    if (!values.dataFimVigencia) nextErrors.dataFimVigencia = 'Data de término é obrigatória'
    else if (values.dataInicioVigencia && values.dataFimVigencia < values.dataInicioVigencia) {
      nextErrors.dataFimVigencia = 'Término não pode ser anterior ao início'
    }
    if (values.numeroInstrumento.length > 100) nextErrors.numeroInstrumento = 'Máximo de 100 caracteres'
    setErrors(nextErrors)
    if (Object.keys(nextErrors).length > 0 || !values.tipo || !values.parceiroId
      || !values.dataInicioVigencia || !values.dataFimVigencia) return

    setSaving(true)
    try {
      await instrumentoService.criar({
        nome: values.nome.trim(),
        tipo: values.tipo,
        numeroInstrumento: values.numeroInstrumento.trim() || null,
        descricao: values.descricao.trim() || null,
        dataInicioVigencia: values.dataInicioVigencia,
        dataFimVigencia: values.dataFimVigencia,
        parceiro: { id: values.parceiroId },
        oportunidade: values.oportunidadeId ? { id: values.oportunidadeId } : null,
      })
      toast.success('Instrumento jurídico cadastrado com sucesso.')
      fecharDialog()
      setLoading(true)
      const dados = await carregarDados()
      setInstrumentos(dados.instrumentos)
      setParceiros(dados.parceiros)
      setOportunidades(dados.oportunidades)
      setAlertasRefreshToken((token) => token + 1)
    } catch (error) {
      toast.error(parseApiError(error).message)
    } finally {
      setLoading(false)
      setSaving(false)
    }
  }

  const tipoLabel = (tipo: TipoInstrumento) =>
    TIPO_INSTRUMENTO_OPTIONS.find((option) => option.value === tipo)?.label ?? tipo

  const abrirProrrogacoes = async (instrumento: InstrumentoJuridico) => {
    setInstrumentoSelecionado(instrumento)
    setNovaDataFim('')
    setJustificativa('')
    setErroProrrogacao('')
    setProrrogacoes([])
    setProrrogacaoOpen(true)
    setLoadingProrrogacoes(true)
    try {
      setProrrogacoes(await instrumentoService.listarProrrogacoes(instrumento.id))
    } catch (error) {
      toast.error(parseApiError(error).message)
    } finally {
      setLoadingProrrogacoes(false)
    }
  }

  const salvarProrrogacao = async (event: FormEvent) => {
    event.preventDefault()
    if (!instrumentoSelecionado) return
    if (!novaDataFim) {
      setErroProrrogacao('Informe a nova data de término')
      return
    }
    if (novaDataFim <= instrumentoSelecionado.dataFimVigencia) {
      setErroProrrogacao('A nova data deve ser posterior ao término vigente')
      return
    }

    setSavingProrrogacao(true)
    setErroProrrogacao('')
    try {
      await instrumentoService.prorrogar(instrumentoSelecionado.id, novaDataFim, justificativa)
      toast.success('Vigência prorrogada e histórico atualizado.')
      const [historico, atualizados] = await Promise.all([
        instrumentoService.listarProrrogacoes(instrumentoSelecionado.id),
        instrumentoService.listar(),
      ])
      setProrrogacoes(historico)
      setInstrumentos(atualizados)
      setAlertasRefreshToken((token) => token + 1)
      setInstrumentoSelecionado(
        atualizados.find((item) => item.id === instrumentoSelecionado.id) ?? null,
      )
      setNovaDataFim('')
      setJustificativa('')
    } catch (error) {
      const message = parseApiError(error).message
      setErroProrrogacao(message)
      toast.error(message)
    } finally {
      setSavingProrrogacao(false)
    }
  }

  return (
    <div className="flex flex-col gap-5">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold">Instrumentos Jurídicos</h1>
        <Button
          type="button"
          icon="pi pi-plus"
          label="Novo Instrumento"
          onClick={() => setDialogOpen(true)}
        />
      </div>

      <AlertasVencimento refreshToken={alertasRefreshToken} />

      <div className="rounded-xl border border-zinc-200 bg-white p-2">
        <DataTable
          value={instrumentos}
          loading={loading}
          dataKey="id"
          paginator
          rows={10}
          rowsPerPageOptions={[10, 25, 50]}
          emptyMessage="Nenhum instrumento jurídico cadastrado."
        >
          <Column field="nome" header="Instrumento" />
          <Column header="Tipo" body={(item: InstrumentoJuridico) => tipoLabel(item.tipo)} />
          <Column header="Número" body={(item: InstrumentoJuridico) => item.numeroInstrumento || '—'} />
          <Column
            header="Vigência"
            body={(item: InstrumentoJuridico) =>
              `${formatDate(item.dataInicioVigencia)} a ${formatDate(item.dataFimVigencia)}`}
          />
          <Column header="Parceiro" body={(item: InstrumentoJuridico) => item.parceiro.razaoSocial} />
          <Column header="Oportunidade" body={(item: InstrumentoJuridico) => item.oportunidade?.titulo || '—'} />
          <Column header="Cadastro" body={(item: InstrumentoJuridico) => formatDate(item.dataCadastro)} />
          <Column
            header="Ações"
            body={(item: InstrumentoJuridico) => (
              <Button
                type="button"
                label="Prorrogações"
                icon="pi pi-history"
                text
                onClick={() => void abrirProrrogacoes(item)}
              />
            )}
          />
        </DataTable>
      </div>

      <Dialog
        header="Cadastrar instrumento jurídico"
        visible={dialogOpen}
        onHide={() => {
          if (!saving) fecharDialog()
        }}
        style={{ width: '42rem', maxWidth: '95vw' }}
      >
        <form onSubmit={salvar} noValidate className="flex flex-col gap-4">
          <FormField label="Nome do instrumento" required error={errors.nome}>
            <InputText
              value={values.nome}
              maxLength={200}
              onChange={(event) => setValue('nome', event.target.value)}
              className={errors.nome ? 'p-invalid' : ''}
            />
          </FormField>
          <FormField label="Tipo de instrumento" required error={errors.tipo}>
            <Dropdown
              value={values.tipo || null}
              options={TIPO_INSTRUMENTO_OPTIONS}
              onChange={(event) => setValue('tipo', event.value ?? '')}
              placeholder="Selecione um tipo..."
              className={errors.tipo ? 'p-invalid' : ''}
            />
          </FormField>
          <FormField label="Parceiro" required error={errors.parceiroId}>
            <Dropdown
              value={values.parceiroId || null}
              options={parceiros}
              optionLabel="razaoSocial"
              optionValue="id"
              onChange={(event) => {
                setValue('parceiroId', event.value ?? '')
                setValue('oportunidadeId', '')
              }}
              placeholder="Selecione um parceiro..."
              className={errors.parceiroId ? 'p-invalid' : ''}
            />
          </FormField>
          <FormField label="Oportunidade vinculada (opcional)">
            <Dropdown
              value={values.oportunidadeId || null}
              options={oportunidadesDoParceiro}
              optionLabel="titulo"
              optionValue="id"
              onChange={(event) => setValue('oportunidadeId', event.value ?? '')}
              placeholder={values.parceiroId ? 'Selecione uma oportunidade...' : 'Selecione primeiro o parceiro'}
              disabled={!values.parceiroId}
              showClear
            />
          </FormField>
          <FormField label="Número do instrumento" error={errors.numeroInstrumento}>
            <InputText
              value={values.numeroInstrumento}
              maxLength={100}
              onChange={(event) => setValue('numeroInstrumento', event.target.value)}
            />
          </FormField>
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <FormField label="Início da vigência" required error={errors.dataInicioVigencia}>
              <InputText
                type="date"
                value={values.dataInicioVigencia}
                onChange={(event) => setValue('dataInicioVigencia', event.target.value)}
                className={errors.dataInicioVigencia ? 'p-invalid' : ''}
              />
            </FormField>
            <FormField label="Término da vigência" required error={errors.dataFimVigencia}>
              <InputText
                type="date"
                value={values.dataFimVigencia}
                min={values.dataInicioVigencia || undefined}
                onChange={(event) => setValue('dataFimVigencia', event.target.value)}
                className={errors.dataFimVigencia ? 'p-invalid' : ''}
              />
            </FormField>
          </div>
          <FormField label="Descrição">
            <InputTextarea
              value={values.descricao}
              rows={3}
              onChange={(event) => setValue('descricao', event.target.value)}
            />
          </FormField>
          <div className="flex justify-end gap-2">
            <Button type="button" label="Cancelar" outlined onClick={fecharDialog} disabled={saving} />
            <Button type="submit" label="Salvar" loading={saving} />
          </div>
        </form>
      </Dialog>

      <Dialog
        header={instrumentoSelecionado ? `Vigência — ${instrumentoSelecionado.nome}` : 'Prorrogação de vigência'}
        visible={prorrogacaoOpen}
        onHide={() => {
          if (!savingProrrogacao) setProrrogacaoOpen(false)
        }}
        style={{ width: '40rem', maxWidth: '95vw' }}
      >
        {instrumentoSelecionado && (
          <div className="flex flex-col gap-5">
            <p className="text-sm text-zinc-700">
              Vigência atual: <strong>{formatDate(instrumentoSelecionado.dataInicioVigencia)}</strong>
              {' a '}
              <strong>{formatDate(instrumentoSelecionado.dataFimVigencia)}</strong>
            </p>
            <section>
              <h3 className="mb-2 font-semibold">Histórico de prorrogações</h3>
              {loadingProrrogacoes ? (
                <p className="text-sm text-zinc-500">Carregando histórico...</p>
              ) : prorrogacoes.length === 0 ? (
                <p className="text-sm text-zinc-500">Nenhuma prorrogação registrada.</p>
              ) : (
                <ol className="relative ml-2 border-l border-zinc-200">
                  {prorrogacoes.map((item) => (
                    <li key={item.id} className="mb-4 ml-5">
                      <p className="text-xs text-zinc-500">
                        {formatDate(item.dataProrrogacao)} · {formatDate(item.dataFimAnterior)}
                        {' → '}
                        {formatDate(item.dataFimNova)}
                      </p>
                      {item.justificativa && <p className="text-sm">{item.justificativa}</p>}
                    </li>
                  ))}
                </ol>
              )}
            </section>
            <form onSubmit={salvarProrrogacao} className="flex flex-col gap-4">
              <FormField label="Nova data de término" required error={erroProrrogacao}>
                <InputText
                  type="date"
                  min={instrumentoSelecionado.dataFimVigencia}
                  value={novaDataFim}
                  onChange={(event) => {
                    setNovaDataFim(event.target.value)
                    setErroProrrogacao('')
                  }}
                  className={erroProrrogacao ? 'p-invalid' : ''}
                />
              </FormField>
              <FormField label="Justificativa (opcional)">
                <InputTextarea
                  rows={3}
                  maxLength={2000}
                  value={justificativa}
                  onChange={(event) => setJustificativa(event.target.value)}
                />
              </FormField>
              <div className="flex justify-end">
                <Button
                  type="submit"
                  label="Prorrogar vigência"
                  icon="pi pi-calendar-plus"
                  loading={savingProrrogacao}
                />
              </div>
            </form>
          </div>
        )}
      </Dialog>
    </div>
  )
}
