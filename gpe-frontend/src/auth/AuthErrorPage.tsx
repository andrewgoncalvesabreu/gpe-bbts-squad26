export function AuthErrorPage() {
  return (
    <main className="flex min-h-screen items-center justify-center bg-zinc-100 p-6">
      <section className="max-w-lg rounded-xl border border-red-200 bg-white p-6 shadow-sm">
        <h1 className="text-xl font-semibold text-zinc-900">Não foi possível iniciar a autenticação</h1>
        <p className="mt-3 text-sm leading-6 text-zinc-600">
          Verifique se o Keycloak está em execução com <code>docker compose up -d keycloak-gpe</code> e tente novamente.
        </p>
      </section>
    </main>
  )
}
