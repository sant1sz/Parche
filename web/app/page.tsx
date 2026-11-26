// Página inicial provisional de la fase 0. Las pantallas reales y la identidad visual
// llegan en la fase 1.
export default function Home() {
  return (
    <main className="flex flex-1 flex-col items-center justify-center gap-3 px-6 text-center">
      <h1 className="text-5xl font-semibold tracking-tight">Parche</h1>
      <p className="max-w-sm text-lg text-zinc-600 dark:text-zinc-400">
        Pedido a la mesa para restaurantes y cafeterías de Medellín.
      </p>
    </main>
  );
}
