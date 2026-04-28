<script lang="ts">
	let { formId = undefined, name = undefined, value = $bindable(0), max = 5, min = 1 } = $props();

	const percentage = $derived(((value - min) / (max - min)) * 100);
</script>

<div class="slider-wrapper flex h-6 items-center">
	<div
		class="visual-track bean-border bg-main-light h-3 w-full overflow-hidden border border-main-border"
	>
		<div class="fill h-full bg-main-mid px-1" style="width: {percentage}%"></div>
	</div>

	<input form={formId} {name} type="range" {min} {max} step="1" bind:value class="coffee-slider" />
</div>

<style>
	.slider-wrapper {
		position: relative;
		width: 100%;
	}

	.visual-track {
		position: absolute;
		width: 100%;
		z-index: 1;
		pointer-events: none;
	}

	.coffee-slider {
		position: relative;
		z-index: 2;
		-webkit-appearance: none;
		appearance: none;
		width: 100%;
		background: transparent; /* makes the native track invisible */
		margin: 0;
		cursor: pointer;
	}

	/* Target the Thumb only - it sits on top of your visual bar */
	.coffee-slider::-webkit-slider-thumb {
		-webkit-appearance: none;
		appearance: none;
		width: 20px;
		height: 20px;
		background: var(--color-main-mid);
		border: 3px solid var(--color-main-border);
		border-radius: 50%;
		margin-top: -2px;
	}

	.coffee-slider::-moz-range-thumb {
		width: 20px;
		height: 20px;
		background: var(--color-main-mid);
		border: 3px solid var(--color-main-border);
		border-radius: 50%;
	}
</style>
