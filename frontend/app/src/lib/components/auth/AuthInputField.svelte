<script lang="ts">
	let { id, label, children } = $props();
</script>

<div class="field">
	<label for={id}>{label}</label>
	{@render children()}
</div>

<style>
	label {
		font-size: var(--text-sm);
	}
	.field {
		display: flex;
		flex-direction: column;
		gap: 0.25rem;
	}
</style>
