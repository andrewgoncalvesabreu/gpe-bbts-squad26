import { useCallback, useEffect, useState } from 'react'
import { Button } from 'primereact/button'
import { Dialog } from 'primereact/dialog'
import { Dropdown } from 'primereact/dropdown'
import { InputTextarea } from 'primereact/inputtextarea'
import { useToast } from '../../../components/common/toastContext'
import { TIPO_HISTORICO_OPTIONS, labelFrom } from '../../../constants/parceiro'
import { historicoParceiroService } from '../../../services/historicoParceiroService'
import type { HistoricoParceiro } from '../../../types/parceiro'
import { parseApiError } from '../../../utils/apiError'
import { formatDateTime } from '../../../utils/format'

export function HistoricoTab({ parceiroId }: { parceiroId: string }) {
  const toast = useToast()
  const [itens, setItens] = useState<HistoricoParceiro[]>([])
  const [loading, setLoading] = useState(true)
  const [dialogOpen, setDialogOpen] = useState(false)
  const [tipo, setTipo] = useState<string>('INTERACAO')
  const [descricao, setDescricao] = useState('')
  const [saving, setSaving] = useState(false)

  const carregar = useCallback(async () => {
    setLoading(true)
    try {
      setItens(await historicoParceiroService.listar(parceiroId))
    } catch (err) {
      toast.error(parseApiError(err).message)
    } finally {
      setLoading(false)
    }
  }, [parceiroId, toast])

  useEffect(() => {
    void carregar()
  }, [carregar])

  const salvar = async () => {
    if (!descricao.trim()) return
    setSaving(true)
    try {
      await historicoParceiroService.adicionar(parceiroId, { tipo, descricao: descricao.trim() })
      toast.success('Registro adicionado ao histórico.')
      setDialogOpen(false)
      setDescricao('')
      await carregar()
    } catch (err) {
      toast.error(parseApiError(err).message)
    } finally {
      setSaving(false)
    }
  }

  return (
    <section className="rounded-xl border border-zinc-200 bg-white p-5">
      <div className="mb-4 flex items-center justify-between">
        <h3 className="text-base font-semibold">Histórico do Parceiro</h3>
        <Button type="button" icon="pi pi-plus" label="Registrar evento" onClick={() => setDialogOpen(true)} />
      </div>

      {loading ? (
        <p className="text-sm text-zinc-500">Carregando histórico...</p>
      ) : itens.length === 0 ? (
        <p className="text-sm text-zinc-500">Nenhum evento registrado para este parceiro.</p>
      ) : (
        <ol className="relative ml-2 border-l border-zinc-200">
          {itens.map((h) => (
            <li key={h.id} className="mb-5 ml-5">
              <span className="absolute -left-[5px] mt-1.5 h-2.5 w-2.5 rounded-full bg-zinc-600" />
              <p className="text-xs text-zinc-400">
                {formatDateTime(h.dataEvento)} · {labelFrom(TIPO_HISTORICO_OPTIONS, h.tipo)}
              </p>
              <p className="text-sm text-zinc-900">{h.descricao}</p>
            </li>
          ))}
        </ol>
      )}

      <Dialog
        header="Registrar evento no histórico"
        visible={dialogOpen}
        onHide={() => setDialogOpen(false)}
        style={{ width: '32rem', maxWidth: '95vw' }}
        footer={
          <div className="flex justify-end gap-2">
            <Button type="button" label="Cancelar" outlined onClick={() => setDialogOpen(false)} />
            <Button type="button" label="Salvar" loading={saving} disabled={!descricao.trim()} onClick={salvar} />
          </div>
        }
      >
        <div className="flex flex-col gap-4">
          <div className="flex flex-col gap-1">
            <label className="text-sm font-medium">Tipo de evento</label>
            <Dropdown value={tipo} options={TIPO_HISTORICO_OPTIONS} onChange={(e) => setTipo(e.value)} />
          </div>
          <div className="flex flex-col gap-1">
            <label className="text-sm font-medium">Descrição</label>
            <InputTextarea
              rows={4}
              value={descricao}
              onChange={(e) => setDescricao(e.target.value)}
              placeholder="Ex.: Reunião de alinhamento sobre o acordo de cooperação."
            />
          </div>
        </div>
      </Dialog>
    </section>
  )
}
