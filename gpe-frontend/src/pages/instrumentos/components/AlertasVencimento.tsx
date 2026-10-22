import { useEffect, useState } from 'react'
import { instrumentoService } from '../../../services/instrumentoService'
import type { InstrumentoAlerta } from '../../../types/instrumento'
import { parseApiError } from '../../../utils/apiError'
import { formatDate } from '../../../utils/format'
import { useToast } from '../../../components/common/toastContext'

export function AlertasVencimento({ refreshToken }: { refreshToken: number }) {
  const toast = useToast()
  const [alertas, setAlertas] = useState<InstrumentoAlerta[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    let ativo = true
    instrumentoService.listarAlertasVencimento()
      .then((resultado) => {
        if (ativo) setAlertas(resultado)
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
  }, [refreshToken, toast])

  if (loading) {
    return <p className="text-sm text-zinc-500">Verificando vencimentos próximos...</p>
  }

  if (alertas.length === 0) {
    return (
      <section className="rounded-xl border border-emerald-200 bg-emerald-50 p-4 text-sm text-emerald-800">
        Nenhum instrumento vence nos próximos 30 dias.
      </section>
    )
  }

  return (
    <section
      aria-label="Alertas de vencimento"
      className="rounded-xl border border-amber-300 bg-amber-50 p-4"
    >
      <h2 className="mb-3 flex items-center gap-2 font-semibold text-amber-950">
        <i className="pi pi-exclamation-triangle" aria-hidden="true" />
        Instrumentos próximos do vencimento
      </h2>
      <ul className="flex flex-col gap-2">
        {alertas.map((alerta) => (
          <li
            key={alerta.instrumentoId}
            className="flex flex-wrap items-center justify-between gap-2 rounded-lg bg-white p-3"
          >
            <div>
              <p className="font-medium text-zinc-900">{alerta.instrumentoNome}</p>
              <p className="text-sm text-zinc-600">{alerta.parceiroNome}</p>
            </div>
            <p className="text-sm font-semibold text-amber-800">
              Vence em {formatDate(alerta.dataVencimento)}
              {' · '}
              {alerta.diasAteVencimento === 0
                ? 'vence hoje'
                : `${alerta.diasAteVencimento} dias`}
            </p>
          </li>
        ))}
      </ul>
    </section>
  )
}
