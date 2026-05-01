<script lang="ts">
	let { children, disabled = false, isPrimary = true } = $props();
</script>

<button
	class="btn-wide mt-4 {isPrimary
		? 'bg-primary'
		: 'bg-error'} font-semibold tracking-wide text-light hover:inset-shadow-md disabled:pointer-events-none disabled:bg-main-mid"
	type="submit"
	{disabled}>{@render children()}</button
>
