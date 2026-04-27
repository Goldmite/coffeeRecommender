<script lang="ts">
	let { headerTxt, children } = $props();
	let dialogElement: HTMLDialogElement;

	export function open() {
		dialogElement.showModal();
	}

	export function close() {
		dialogElement.close();
	}
</script>

<dialog
	bind:this={dialogElement}
	class="modal bean-border border-2 border-secondary"
	onclick={(e) => e.target === dialogElement && close()}
>
	<div class="bg-main p-6 text-main-text">
		<header class="mb-5 flex items-start justify-between">
			<h2>{headerTxt}</h2>
			<button onclick={close}>✕</button>
		</header>
		{@render children()}
	</div>
</dialog>

<style>
	.modal {
		position: fixed;
		inset: 0;
		margin: auto;
		padding: 0;
		width: 90%;
		max-width: 600px;
	}
	.modal::backdrop {
		background-color: var(--color-dark);
		opacity: 0.5;
	}
</style>
