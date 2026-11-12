import { useCallback, useEffect, useRef, useState, type FormEvent } from 'react'
import { Button } from 'primereact/button'
import { InputText } from 'primereact/inputtext'
import { InputTextarea } from 'primereact/inputtextarea'
import { useToast } from '../../../components/common/toastContext'
import { documentoService } from '../../../services/documentoService'
import { projetoService } from '../../../services/projetoService'
import type { AssinaturaDocumento, Documento, DocumentoVersao } from '../../../types/documento'
import type { Parceria } from '../../../types/projeto'
import { parseApiError } from '../../../utils/apiError'
import { formatDateTime } from '../../../utils/format'

const MAX_FILE_SIZE = 20 * 1024 * 1024
const ACCEPTED_TYPES = '.pdf,.docx,.xlsx,.png,.jpg,.jpeg'

export function DocumentosTab({ parceiroId }: { parceiroId: string }) {
  const toast = useToast()
  const inputRef = useRef<HTMLInputElement>(null)
  const [documentos, setDocumentos] = useState<Documento[]>([])
  const [parcerias, setParcerias] = useState<Parceria[]>([])
  const [parceriaId, setParceriaId] = useState('')
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [file, setFile] = useState<File | null>(null)
  const [nome, setNome] = useState('')
  const [descricao, setDescricao] = useState('')
  const [matricula, setMatricula] = useState('')
  const [versoes, setVersoes] = useState<Record<string, DocumentoVersao[]>>({})
  const [assinaturas, setAssinaturas] = useState<Record<string, AssinaturaDocumento[]>>({})
  const [versionFiles, setVersionFiles] = useState<Record<string, File | null>>({})

  const carregar = useCallback(async () => {
    try {
      const [documentosData, parceriasData] = await Promise.all([
        documentoService.listarPorParceiro(parceiroId),
        projetoService.listarParcerias(parceiroId),
      ])
      setDocumentos(documentosData)
      setParcerias(parceriasData)
      setParceriaId((current) => current || parceriasData[0]?.id || '')
    } catch (error) {
      toast.error(parseApiError(error).message)
    } finally {
      setLoading(false)
    }
  }, [parceiroId, toast])

  useEffect(() => {
    void carregar()
  }, [carregar])

  const validarArquivo = (value: File | null) => {
    if (!value) {
      toast.error('Selecione um arquivo.')
      return false
    }
    if (value.size > MAX_FILE_SIZE) {
      toast.error('O arquivo deve ter no máximo 20 MB.')
      return false
    }
    const extension = value.name.split('.').pop()?.toLowerCase()
    if (!extension || !['pdf', 'docx', 'xlsx', 'png', 'jpg', 'jpeg'].includes(extension)) {
      toast.error('Formato não permitido. Use PDF, DOCX, XLSX, PNG ou JPG.')
      return false
    }
    return true
  }

  const anexar = async (event: FormEvent) => {
    event.preventDefault()
    if (!validarArquivo(file)) return
    if (!parceriaId || !nome.trim() || !matricula.trim()) {
      toast.error('Selecione uma parceria e informe o nome do documento e a matrícula do responsável.')
      return
    }
    setSaving(true)
    try {
      await documentoService.anexar(parceiroId, parceriaId, file!, nome.trim(), descricao.trim(), matricula.trim())
      toast.success('Documento anexado.')
      setFile(null)
      setNome('')
      setDescricao('')
      if (inputRef.current) inputRef.current.value = ''
      setLoading(true)
      await carregar()
    } catch (error) {
      toast.error(parseApiError(error).message)
    } finally {
      setSaving(false)
    }
  }

  const baixar = async (documento: Documento, version?: number, filename?: string) => {
    try {
      const blob = await documentoService.baixarBlob(documento.id, version)
      const url = URL.createObjectURL(blob)
      const anchor = window.document.createElement('a')
      anchor.href = url
      anchor.download = filename ?? documento.versao?.nomeArquivo ?? documento.nomeArquivoAtual ?? documento.nome
      anchor.click()
      URL.revokeObjectURL(url)
    } catch (error) {
      toast.error(parseApiError(error).message)
    }
  }

  const novaVersao = async (documento: Documento) => {
    const versionFile = versionFiles[documento.id] ?? null
    if (!validarArquivo(versionFile)) return
    if (!matricula.trim()) {
      toast.error('Informe a matrícula do responsável.')
      return
    }
    setSaving(true)
    try {
      await documentoService.adicionarVersao(documento.id, versionFile!, matricula.trim())
      toast.success('Nova versão armazenada; a versão anterior foi preservada.')
      setVersionFiles((current) => ({ ...current, [documento.id]: null }))
      setLoading(true)
      await carregar()
      const currentVersions = await documentoService.listarVersoes(documento.id)
      setVersoes((current) => ({ ...current, [documento.id]: currentVersions }))
    } catch (error) {
      toast.error(parseApiError(error).message)
    } finally {
      setSaving(false)
    }
  }

  const listarVersoes = async (documentoId: string) => {
    try {
      const currentVersions = await documentoService.listarVersoes(documentoId)
      setVersoes((current) => ({ ...current, [documentoId]: currentVersions }))
    } catch (error) {
      toast.error(parseApiError(error).message)
    }
  }

  const assinar = async (documento: Documento) => {
    if (!matricula.trim()) {
      toast.error('Informe a matrícula do responsável.')
      return
    }
    setSaving(true)
    try {
      await documentoService.enviarAssinatura(documento.id, matricula.trim())
      const history = await documentoService.listarAssinaturas(documento.id)
      setAssinaturas((current) => ({
        ...current,
        [documento.id]: history,
      }))
      toast.success('Solicitação encaminhada ao serviço mock de assinatura.')
      setLoading(true)
      await carregar()
    } catch (error) {
      toast.error(parseApiError(error).message)
      try {
        const history = await documentoService.listarAssinaturas(documento.id)
        setAssinaturas((current) => ({
          ...current,
          [documento.id]: history,
        }))
        setLoading(true)
        await carregar()
      } catch (historyError) {
        toast.error(parseApiError(historyError).message)
      }
    } finally {
      setSaving(false)
    }
  }

  const listarAssinaturas = async (documentoId: string) => {
    try {
      const history = await documentoService.listarAssinaturas(documentoId)
      setAssinaturas((current) => ({
        ...current,
        [documentoId]: history,
      }))
    } catch (error) {
      toast.error(parseApiError(error).message)
    }
  }

  const inputClass = 'w-full rounded-md border border-zinc-300 px-3 py-2 text-sm'

  return (
    <section className="flex flex-col gap-5 rounded-xl border border-zinc-200 bg-white p-5">
      <header>
        <h3 className="text-base font-semibold">Documentos das parcerias</h3>
        <p className="mt-1 text-sm text-zinc-500">PDF, DOCX, XLSX, PNG ou JPG; limite de 20 MB por arquivo.</p>
      </header>

      <form onSubmit={anexar} className="grid grid-cols-1 gap-3 rounded-lg bg-zinc-50 p-4 md:grid-cols-2">
        <h4 className="font-medium md:col-span-2">Anexar documento</h4>
        <label className="flex flex-col gap-1 text-sm">
          Parceria
          <select
            className={inputClass}
            value={parceriaId}
            onChange={(event) => setParceriaId(event.target.value)}
            disabled={parcerias.length === 0}
          >
            <option value="">Selecione uma parceria</option>
            {parcerias.map((parceria) => (
              <option key={parceria.id} value={parceria.id}>{parceria.nome}</option>
            ))}
          </select>
        </label>
        <label className="flex flex-col gap-1 text-sm">
          Nome do documento
          <InputText value={nome} maxLength={200} onChange={(event) => setNome(event.target.value)} />
        </label>
        <label className="flex flex-col gap-1 text-sm">
          Matrícula do responsável
          <InputText value={matricula} maxLength={50} onChange={(event) => setMatricula(event.target.value)} />
        </label>
        <label className="flex flex-col gap-1 text-sm md:col-span-2">
          Arquivo
          <input
            ref={inputRef}
            className={inputClass}
            type="file"
            accept={ACCEPTED_TYPES}
            onChange={(event) => setFile(event.target.files?.[0] ?? null)}
          />
        </label>
        <label className="flex flex-col gap-1 text-sm md:col-span-2">
          Descrição (opcional)
          <InputTextarea rows={2} value={descricao} onChange={(event) => setDescricao(event.target.value)} />
        </label>
        <div className="flex items-center justify-between gap-2 md:col-span-2">
          <span className="text-xs text-zinc-500">{file ? `${file.name} · ${(file.size / 1024 / 1024).toFixed(2)} MB` : 'Nenhum arquivo selecionado'}</span>
          <Button type="submit" label="Anexar" icon="pi pi-upload" loading={saving} disabled={!file || !parceriaId || !nome.trim() || !matricula.trim()} />
        </div>
      </form>

      {parcerias.length === 0 && !loading && (
        <p className="text-sm text-amber-700">Cadastre uma parceria formalizada antes de anexar documentos.</p>
      )}
      {loading ? <p className="text-sm text-zinc-500">Carregando documentos...</p>
        : documentos.length === 0 ? <p className="text-sm text-zinc-500">Nenhum documento anexado.</p>
          : documentos.map((documento) => (
            <article key={documento.id} className="rounded-lg border border-zinc-200 p-4">
              <div className="flex flex-wrap items-start justify-between gap-3">
                <div>
                  <h4 className="font-semibold">{documento.nome}</h4>
                  {documento.parceriaNome && (
                    <p className="text-sm text-zinc-600">Parceria: {documento.parceriaNome}</p>
                  )}
                  {documento.descricao && <p className="mt-1 text-sm text-zinc-600">{documento.descricao}</p>}
                  <p className="mt-1 text-xs text-zinc-500">
                    Versão atual: {documento.versaoAtual} · Assinatura: {documento.statusAssinatura}
                  </p>
                </div>
                <div className="flex flex-wrap gap-2">
                  <Button type="button" label="Baixar atual" icon="pi pi-download" outlined onClick={() => void baixar(documento)} />
                  <Button type="button" label="Versões" icon="pi pi-history" text onClick={() => void listarVersoes(documento.id)} />
                  <Button type="button" label="Assinaturas" icon="pi pi-pencil" text onClick={() => void listarAssinaturas(documento.id)} />
                  <Button type="button" label="Enviar para assinatura" icon="pi pi-send" onClick={() => void assinar(documento)} loading={saving} />
                </div>
              </div>

              <div className="mt-3 flex flex-wrap items-end gap-2">
                <label className="flex flex-col gap-1 text-xs text-zinc-600">
                  Nova versão
                  <input
                    className={inputClass}
                    type="file"
                    accept={ACCEPTED_TYPES}
                    onChange={(event) => setVersionFiles((current) => ({
                      ...current,
                      [documento.id]: event.target.files?.[0] ?? null,
                    }))}
                  />
                </label>
                <Button
                  type="button"
                  label="Enviar nova versão"
                  icon="pi pi-upload"
                  outlined
                  disabled={!versionFiles[documento.id] || !matricula.trim()}
                  loading={saving}
                  onClick={() => void novaVersao(documento)}
                />
              </div>

              {versoes[documento.id] && (
                <div className="mt-4 border-t border-zinc-100 pt-3">
                  <h5 className="mb-2 text-sm font-medium">Histórico de versões</h5>
                  <ul className="flex flex-col gap-2">
                    {versoes[documento.id].map((version) => (
                      <li key={version.id} className="flex flex-wrap items-center justify-between gap-2 text-sm">
                        <span>
                          v{version.numeroVersao} · {version.nomeArquivo} ·                           {formatDateTime(version.dataEnvio)}
                          {' · '}Responsável: {version.responsavelMatricula}
                        </span>
                        <Button
                          type="button"
                          label="Baixar"
                          icon="pi pi-download"
                          text
                          onClick={() => void baixar(documento, version.numeroVersao, version.nomeArquivo)}
                        />
                      </li>
                    ))}
                  </ul>
                </div>
              )}

              {assinaturas[documento.id] && (
                <div className="mt-4 border-t border-zinc-100 pt-3">
                  <h5 className="mb-2 text-sm font-medium">Histórico de assinatura</h5>
                  {assinaturas[documento.id].length === 0 ? (
                    <p className="text-sm text-zinc-500">Nenhuma solicitação de assinatura.</p>
                  ) : (
                    <ul className="flex flex-col gap-2">
                      {assinaturas[documento.id].map((item) => (
                        <li key={item.id} className="text-sm">
                          v{item.numeroVersao} · {item.provedor} · {item.status} · {formatDateTime(item.dataEvento)}
                          {' · '}Responsável: {item.responsavelMatricula}
                          {item.protocolo && <span> · Protocolo: {item.protocolo}</span>}
                          {item.detalhe && <p className="text-xs text-zinc-500">{item.detalhe}</p>}
                        </li>
                      ))}
                    </ul>
                  )}
                </div>
              )}
            </article>
          ))}
    </section>
  )
}
