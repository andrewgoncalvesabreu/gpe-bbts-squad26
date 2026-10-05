import { useMemo, useRef, type ReactNode } from 'react'
import { Toast } from 'primereact/toast'
import { ToastContext, type ToastApi } from './toastContext'

export function ToastProvider({ children }: { children: ReactNode }) {
  const toastRef = useRef<Toast>(null)

  const api = useMemo<ToastApi>(
    () => ({
      success: (detail) => toastRef.current?.show({ severity: 'success', summary: 'Sucesso', detail, life: 3500 }),
      error: (detail) => toastRef.current?.show({ severity: 'error', summary: 'Erro', detail, life: 6000 }),
      info: (detail) => toastRef.current?.show({ severity: 'info', summary: 'Aviso', detail, life: 3500 }),
    }),
    [],
  )

  return (
    <ToastContext.Provider value={api}>
      {children}
      <Toast ref={toastRef} position="top-right" />
    </ToastContext.Provider>
  )
}
