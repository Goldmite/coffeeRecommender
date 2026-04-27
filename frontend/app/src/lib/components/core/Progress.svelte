<script lang="ts">
	import { cubicOut } from 'svelte/easing';
	import { Tween } from 'svelte/motion';

	let { value, max = 5 } = $props();

	const x = $derived(value / max);

	const progress = new Tween(0, {
		duration: 400,
		easing: cubicOut,
	});

	$effect(() => {
		progress.target = x;
	});

	const percentage = $derived(progress.current * 100);
	const colorClass = $derived(() => {
		if (percentage <= 25) {
			return 'bg-error';
		}
		if (percentage <= 75) {
			return 'bg-secondary';
		}
		return 'bg-primary';
	});
</script>

<div class="my-2 w-full">
	<div class="track bean-border h-3 w-full overflow-hidden border border-main-border">
		<!-- bg-error bg-secondary bg-primary -->
		<div
			class="fill {colorClass()} h-full transition-colors"
			style="width: {percentage}%"
			role="progressbar"
			aria-valuenow={value}
			aria-valuemax={max}
		></div>
	</div>
</div>

<style>
	.fill {
		will-change: width;
	}
</style>
