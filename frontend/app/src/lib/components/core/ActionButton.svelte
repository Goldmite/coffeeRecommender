<script lang="ts">
	let { children, formAction, disabled = false } = $props();
</script>

<button
	class="btn-wide mt-4 bg-secondary font-semibold tracking-wide text-light"
	type="submit"
	formaction={formAction}
	{disabled}>{@render children()}</button
>

<style>
	button[type='submit']:hover {
		box-shadow: var(--inset-shadow-md);
	}
</style>
